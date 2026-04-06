package makisimperium.ffa;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.TextFormat;
import makisimperium.ffa.arena.model.ArenaStatus;
import makisimperium.ffa.arena.persistence.ArenaRepository;
import makisimperium.ffa.arena.persistence.JsonArenaRepository;
import makisimperium.ffa.arena.service.ArenaCatalogService;
import makisimperium.ffa.arena.service.ArenaSessionService;
import makisimperium.ffa.arena.service.SafeZonePlacementService;
import makisimperium.ffa.command.FfaAdminCommand;
import makisimperium.ffa.command.FfaCommand;
import makisimperium.ffa.config.ConfigurationService;
import makisimperium.ffa.config.PluginConfiguration;
import makisimperium.ffa.config.StorageBackend;
import makisimperium.ffa.kit.persistence.JsonKitRepository;
import makisimperium.ffa.kit.persistence.KitRepository;
import makisimperium.ffa.kit.service.KitApplicationService;
import makisimperium.ffa.kit.service.KitRegistry;
import makisimperium.ffa.kit.service.KitSelectionService;
import makisimperium.ffa.i18n.LocalizationService;
import makisimperium.ffa.listener.ArenaProtectionListener;
import makisimperium.ffa.listener.FormInteractionListener;
import makisimperium.ffa.listener.PlayerLifecycleListener;
import makisimperium.ffa.listener.ZonePlacementListener;
import makisimperium.ffa.player.service.PlayerProfileService;
import makisimperium.ffa.player.storage.JsonPlayerProfileRepository;
import makisimperium.ffa.player.storage.MySqlPlayerProfileRepository;
import makisimperium.ffa.player.storage.PlayerProfileRepository;
import makisimperium.ffa.scoreboard.FfaScoreboardService;
import makisimperium.ffa.ui.form.FfaFormController;
import makisimperium.ffa.util.JsonFileService;

import java.nio.file.Path;

public final class FFABootstrap extends PluginBase {

    private static final int MYSQL_RECONNECT_INTERVAL_TICKS = 20 * 15;
    private static final int MYSQL_MAX_RECONNECT_ATTEMPTS = 3;

    private PluginConfiguration pluginConfiguration;
    private JsonFileService jsonFileService;
    private LocalizationService localizationService;
    private StorageBackend resolvedStorageBackend = StorageBackend.JSON;
    private boolean strictMySqlMode;
    private boolean databaseOutageMode;
    private int reconnectAttempts;
    private TaskHandler mysqlReconnectTask;
    private final Object databaseStateLock = new Object();

    private PlayerProfileService playerProfileService;
    private ArenaCatalogService arenaCatalogService;
    private ArenaSessionService arenaSessionService;
    private SafeZonePlacementService safeZonePlacementService;
    private KitRegistry kitRegistry;
    private KitSelectionService kitSelectionService;
    private KitApplicationService kitApplicationService;
    private FfaScoreboardService scoreboardService;
    private FfaFormController formController;

    @Override
    public void onEnable() {
        try {
            bootstrap();
            getLogger().info("FFAGameV2 has been enabled.");
        } catch (Exception exception) {
            if (strictMySqlMode && resolvedStorageBackend == StorageBackend.MYSQL) {
                getLogger().warning("FFAGameV2 startup halted because MySQL is unreachable.");
                getLogger().warning("Set storageBackend to JSON in config.json and restart.");
            } else {
                getLogger().error("Failed to enable FFAGameV2 plugin", exception);
            }
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        stopReconnectTask();
        if (scoreboardService != null) {
            scoreboardService.stop();
        }
        if (playerProfileService != null) {
            playerProfileService.persistAllCachedProfiles();
            playerProfileService.closeRepository();
        }
    }

    private void bootstrap() throws Exception {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("Unable to create plugin data folder");
        }

        this.jsonFileService = new JsonFileService();
        this.localizationService = new LocalizationService(this, jsonFileService, getDataFolder().toPath());
        this.localizationService.loadOrCreate();
        ConfigurationService configurationService = new ConfigurationService(this, jsonFileService);
        this.pluginConfiguration = configurationService.loadOrCreate();

        Path dataPath = getDataFolder().toPath();

        ArenaRepository arenaRepository = new JsonArenaRepository(jsonFileService, dataPath.resolve("arenas"));
        this.arenaCatalogService = new ArenaCatalogService(this, arenaRepository);
        this.arenaCatalogService.loadArenas();

        PlayerProfileRepository playerProfileRepository;
        try {
            playerProfileRepository = createPlayerRepository(pluginConfiguration, dataPath);
        } catch (Exception exception) {
            if (strictMySqlMode) {
                forceArenasOffline();
                getLogger().warning("MySQL could not be reached after 3 attempts.");
                getLogger().warning("Switch storageBackend to JSON in config.json and restart the server.");
            }
            throw exception;
        }
        this.playerProfileService = new PlayerProfileService(
                this,
                playerProfileRepository,
                pluginConfiguration.getScoreboard().isEnabledByDefault(),
                strictMySqlMode,
                this::handleDatabaseOutageTriggered
        );

        KitRepository kitRepository = new JsonKitRepository(jsonFileService, dataPath.resolve("kits"));
        this.kitRegistry = new KitRegistry(this, kitRepository);
        this.kitRegistry.loadKits();
        this.kitSelectionService = new KitSelectionService(kitRegistry, playerProfileService);
        this.kitApplicationService = new KitApplicationService(kitRegistry);

        this.arenaSessionService = new ArenaSessionService(
                this,
                arenaCatalogService,
                kitSelectionService,
                kitApplicationService,
                playerProfileService
        );
        this.safeZonePlacementService = new SafeZonePlacementService(arenaCatalogService, playerProfileService);

        this.scoreboardService = new FfaScoreboardService(this, playerProfileService, arenaSessionService);
        this.scoreboardService.start(pluginConfiguration.getScoreboard().getUpdateIntervalTicks());
        this.formController = new FfaFormController(
                arenaCatalogService,
                arenaSessionService,
                safeZonePlacementService,
                kitRegistry,
                kitSelectionService,
                kitApplicationService,
                playerProfileService,
                scoreboardService,
                localizationService
        );

        getServer().getPluginManager().registerEvents(
                new PlayerLifecycleListener(this, playerProfileService, arenaSessionService, scoreboardService, formController),
                this
        );
        getServer().getPluginManager().registerEvents(new ArenaProtectionListener(arenaSessionService), this);
        getServer().getPluginManager().registerEvents(new FormInteractionListener(formController), this);
        getServer().getPluginManager().registerEvents(new ZonePlacementListener(safeZonePlacementService, formController), this);

        getServer().getCommandMap().register("ffa", new FfaCommand(formController));
        getServer().getCommandMap().register("ffaadmin", new FfaAdminCommand(formController));

        for (Player player : getServer().getOnlinePlayers().values()) {
            playerProfileService.loadProfile(player);
            scoreboardService.handleJoin(player);
        }
    }

    private PlayerProfileRepository createPlayerRepository(PluginConfiguration configuration, Path dataPath) throws Exception {
        StorageBackend storageBackend = configuration.resolveStorageBackend();
        this.resolvedStorageBackend = storageBackend;
        this.strictMySqlMode = false;
        if (storageBackend == StorageBackend.JSON) {
            getLogger().warning("Player storage backend is set to JSON. MySQL remains the recommended production option.");
            this.resolvedStorageBackend = StorageBackend.JSON;
            return new JsonPlayerProfileRepository(jsonFileService, dataPath.resolve("players"));
        }

        if (!configuration.getDatabase().isConfigured()) {
            getLogger().info("MySQL is not configured yet. Please update config.json database settings and restart the server.");
            getLogger().info("Using JSON player storage until MySQL is configured.");
            this.resolvedStorageBackend = StorageBackend.JSON;
            return new JsonPlayerProfileRepository(jsonFileService, dataPath.resolve("players"));
        }

        this.strictMySqlMode = true;
        this.resolvedStorageBackend = StorageBackend.MYSQL;

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= MYSQL_MAX_RECONNECT_ATTEMPTS; attempt++) {
            try {
                return new MySqlPlayerProfileRepository(configuration.getDatabase());
            } catch (Exception exception) {
                lastFailure = exception;
                getLogger().warning("MySQL connection attempt " + attempt + "/" + MYSQL_MAX_RECONNECT_ATTEMPTS + " failed.");
                if (attempt < MYSQL_MAX_RECONNECT_ATTEMPTS) {
                    getLogger().warning("Retrying MySQL connection in 15 seconds...");
                    try {
                        Thread.sleep(15000L);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw interruptedException;
                    }
                }
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
        }
        throw new IllegalStateException("MySQL initialization failed without exception details.");
    }

    private void handleDatabaseOutageTriggered() {
        if (!strictMySqlMode || resolvedStorageBackend != StorageBackend.MYSQL) {
            return;
        }

        synchronized (databaseStateLock) {
            if (databaseOutageMode) {
                return;
            }
            databaseOutageMode = true;
            reconnectAttempts = 0;
        }

        forceArenasOffline();
        forcePlayersOutOfArenas();
        if (scoreboardService != null) {
            scoreboardService.refreshAll();
        }
        if (playerProfileService != null) {
            playerProfileService.setDataAvailable(false);
        }

        notifyOnlinePlayers(TextFormat.RED + "FFA data service is currently unavailable. Stats are offline.");
        startReconnectTask();
    }

    private void startReconnectTask() {
        if (mysqlReconnectTask != null) {
            return;
        }
        mysqlReconnectTask = getServer().getScheduler().scheduleRepeatingTask(this, this::attemptMySqlReconnect, MYSQL_RECONNECT_INTERVAL_TICKS);
    }

    private void stopReconnectTask() {
        if (mysqlReconnectTask != null) {
            mysqlReconnectTask.cancel();
            mysqlReconnectTask = null;
        }
    }

    private void attemptMySqlReconnect() {
        if (!strictMySqlMode || resolvedStorageBackend != StorageBackend.MYSQL || playerProfileService == null) {
            stopReconnectTask();
            return;
        }

        boolean reachable = playerProfileService.pingRepository();
        if (reachable) {
            synchronized (databaseStateLock) {
                databaseOutageMode = false;
                reconnectAttempts = 0;
            }
            playerProfileService.setDataAvailable(true);
            stopReconnectTask();
            getLogger().info("MySQL connection restored. Stats are available again.");
            getLogger().info("Arenas stay offline until an admin sets statuses back to Live.");
            notifyOnlinePlayers(TextFormat.YELLOW + "Database connection restored. Arenas remain offline until re-enabled by admin.");
            return;
        }

        int currentAttempt;
        synchronized (databaseStateLock) {
            reconnectAttempts++;
            currentAttempt = reconnectAttempts;
        }

        getLogger().warning("MySQL reconnect attempt " + currentAttempt + "/" + MYSQL_MAX_RECONNECT_ATTEMPTS + " failed.");
        if (currentAttempt < MYSQL_MAX_RECONNECT_ATTEMPTS) {
            return;
        }

        stopReconnectTask();
        getLogger().warning("MySQL remains unreachable. Please switch storageBackend to JSON and restart the server.");
        notifyOnlinePlayers(TextFormat.RED + "Database unreachable. Ask an admin to switch to JSON storage.");
        getServer().getPluginManager().disablePlugin(this);
    }

    private void forceArenasOffline() {
        if (arenaCatalogService == null) {
            return;
        }
        int updated = arenaCatalogService.setAllArenasStatus(ArenaStatus.DISABLED);
        getLogger().warning("Database outage mode active. " + updated + " arena(s) set to Offline.");
    }

    private void forcePlayersOutOfArenas() {
        if (arenaSessionService == null) {
            return;
        }
        for (Player player : getServer().getOnlinePlayers().values()) {
            if (arenaSessionService.isPlayerInArena(player.getUniqueId())) {
                arenaSessionService.leaveArena(player, true);
            }
        }
    }

    private void notifyOnlinePlayers(String message) {
        for (Player player : getServer().getOnlinePlayers().values()) {
            player.sendMessage(message);
        }
    }
}



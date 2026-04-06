package makisimperium.ffa.scoreboard;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.scoreboard.Scoreboard;
import cn.nukkit.utils.TextFormat;
import makisimperium.ffa.arena.model.ArenaDefinition;
import makisimperium.ffa.arena.service.ArenaSessionService;
import makisimperium.ffa.player.model.PlayerLanguage;
import makisimperium.ffa.player.model.PlayerStatistics;
import makisimperium.ffa.player.service.PlayerProfileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FfaScoreboardService {

    private final PluginBase plugin;
    private final PlayerProfileService playerProfileService;
    private final ArenaSessionService arenaSessionService;
    private final ConcurrentHashMap<UUID, Scoreboard> scoreboardsByPlayer = new ConcurrentHashMap<>();

    private TaskHandler updateTask;

    public FfaScoreboardService(
            PluginBase plugin,
            PlayerProfileService playerProfileService,
            ArenaSessionService arenaSessionService
    ) {
        this.plugin = plugin;
        this.playerProfileService = playerProfileService;
        this.arenaSessionService = arenaSessionService;
    }

    public void start(int updateIntervalTicks) {
        int interval = Math.max(1, updateIntervalTicks);
        this.updateTask = plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, this::refreshAll, interval);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers().values()) {
            removeScoreboard(onlinePlayer);
        }
        scoreboardsByPlayer.clear();
    }

    public void refreshPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }
        if (!playerProfileService.isDataAvailable()) {
            removeScoreboard(player);
            return;
        }

        if (!playerProfileService.isScoreboardEnabled(player.getUniqueId())) {
            removeScoreboard(player);
            return;
        }

        Optional<ArenaDefinition> arenaDefinition = arenaSessionService.getArenaForPlayer(player.getUniqueId());
        if (arenaDefinition.isEmpty()) {
            removeScoreboard(player);
            return;
        }

        Scoreboard scoreboard = scoreboardsByPlayer.computeIfAbsent(player.getUniqueId(), uniqueId -> {
            String objectiveName = "ffa_" + uniqueId.toString().substring(0, 8);
            Scoreboard created = new Scoreboard(objectiveName, Scoreboard.SortOrder.DESCENDING, Scoreboard.DisplaySlot.SIDEBAR);
            created.showTo(player);
            return created;
        });

        PlayerStatistics statistics = playerProfileService.getStatisticsOrDefault(player.getUniqueId());
        PlayerLanguage language = playerProfileService.getLanguage(player.getUniqueId());
        String arenaName = arenaDefinition.map(ArenaDefinition::getDisplayName).orElse("-");
        int arenaPlayers = arenaDefinition.map(arena -> arenaSessionService.getArenaPlayerCount(arena.getArenaId())).orElse(0);
        int protectionSeconds = arenaSessionService.getRespawnProtectionSeconds(player.getUniqueId());

        List<String> lines = new ArrayList<>();
        if (language == PlayerLanguage.GERMAN) {
            lines.add(TextFormat.GOLD + "" + TextFormat.BOLD + "FFA ARENA");
            lines.add(TextFormat.GRAY + "Arena " + TextFormat.WHITE + arenaName);
            lines.add(TextFormat.GRAY + "Spieler " + TextFormat.WHITE + arenaPlayers);
            lines.add(TextFormat.GRAY + "Kills " + TextFormat.WHITE + statistics.getKills());
            lines.add(TextFormat.GRAY + "Tode " + TextFormat.WHITE + statistics.getDeaths());
            lines.add(TextFormat.GRAY + "Streak " + TextFormat.WHITE + statistics.getCurrentKillstreak());
            if (protectionSeconds > 0) {
                lines.add(TextFormat.GRAY + "Schutz " + TextFormat.WHITE + protectionSeconds + "s");
            }
        } else {
            lines.add(TextFormat.GOLD + "" + TextFormat.BOLD + "FFA ARENA");
            lines.add(TextFormat.GRAY + "Arena " + TextFormat.WHITE + arenaName);
            lines.add(TextFormat.GRAY + "Players " + TextFormat.WHITE + arenaPlayers);
            lines.add(TextFormat.GRAY + "Kills " + TextFormat.WHITE + statistics.getKills());
            lines.add(TextFormat.GRAY + "Deaths " + TextFormat.WHITE + statistics.getDeaths());
            lines.add(TextFormat.GRAY + "Streak " + TextFormat.WHITE + statistics.getCurrentKillstreak());
            if (protectionSeconds > 0) {
                lines.add(TextFormat.GRAY + "Shield " + TextFormat.WHITE + protectionSeconds + "s");
            }
        }

        scoreboard.holdUpdates();
        scoreboard.clear();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index) + hiddenSuffix(index);
            scoreboard.setScore(line, 0);
        }
        scoreboard.unholdUpdates();
    }

    public void refreshAll() {
        for (Player player : plugin.getServer().getOnlinePlayers().values()) {
            refreshPlayer(player);
        }
    }

    public void handleJoin(Player player) {
        refreshPlayer(player);
    }

    public void handleQuit(Player player) {
        removeScoreboard(player);
    }

    private void removeScoreboard(Player player) {
        Scoreboard scoreboard = scoreboardsByPlayer.remove(player.getUniqueId());
        if (scoreboard != null) {
            scoreboard.hideFor(player);
        }
    }

    private String hiddenSuffix(int index) {
        return "\u00A7" + Integer.toHexString(index % 16);
    }
}



package makisimperium.ffa.ui.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseData;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import makisimperium.ffa.arena.model.ArenaDefinition;
import makisimperium.ffa.arena.model.ArenaStatus;
import makisimperium.ffa.arena.service.ArenaCatalogService;
import makisimperium.ffa.arena.service.ArenaSessionService;
import makisimperium.ffa.arena.service.SafeZonePlacementService;
import makisimperium.ffa.i18n.LocalizationService;
import makisimperium.ffa.kit.model.KitDefinition;
import makisimperium.ffa.kit.model.KitItem;
import makisimperium.ffa.kit.service.KitApplicationService;
import makisimperium.ffa.kit.service.KitRegistry;
import makisimperium.ffa.kit.service.KitSelectionService;
import makisimperium.ffa.messaging.FfaMessageFormatter;
import makisimperium.ffa.player.model.PlayerLanguage;
import makisimperium.ffa.player.model.PlayerProfile;
import makisimperium.ffa.player.model.PlayerStatistics;
import makisimperium.ffa.player.service.PlayerProfileService;
import makisimperium.ffa.scoreboard.FfaScoreboardService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FfaFormController {

    private static final int FORM_PLAYER_MAIN = 1100;
    private static final int FORM_PLAYER_ARENA_MENU = 1101;
    private static final int FORM_PLAYER_ARENA_SELECT = 1102;
    private static final int FORM_PLAYER_ARENA_OVERVIEW = 1103;
    private static final int FORM_PLAYER_KIT_MENU = 1104;
    private static final int FORM_PLAYER_KIT_SELECT = 1105;
    private static final int FORM_PLAYER_STATS = 1106;
    private static final int FORM_PLAYER_SETTINGS = 1107;
    private static final int FORM_PLAYER_ARENA_JOINED = 1108;
    private static final int FORM_PLAYER_KIT_CURRENT = 1109;
    private static final int FORM_PLAYER_IN_ARENA_COMMAND = 1110;
    private static final int FORM_PLAYER_STATS_OVERVIEW = 1111;
    private static final int FORM_PLAYER_STATS_GLOBAL = 1112;
    private static final int FORM_PLAYER_STATS_ARENA = 1113;
    private static final int FORM_PLAYER_STATS_UNAVAILABLE = 1114;

    private static final int FORM_ADMIN_MAIN = 2100;
    private static final int FORM_ADMIN_ARENA_MENU = 2101;
    private static final int FORM_ADMIN_ARENA_CREATE = 2102;
    private static final int FORM_ADMIN_ARENA_DELETE = 2103;
    private static final int FORM_ADMIN_ARENA_ADD_SPAWN = 2104;
    private static final int FORM_ADMIN_ARENA_STATUS = 2105;
    private static final int FORM_ADMIN_ARENA_STATUS_VALUE = 2106;
    private static final int FORM_ADMIN_ZONE_MENU = 2107;
    private static final int FORM_ADMIN_ZONE_SAFE_MODE = 2108;
    private static final int FORM_ADMIN_ARENA_LIST = 2110;
    private static final int FORM_ADMIN_ARENA_DELETE_CONFIRM = 2111;

    private static final int FORM_ADMIN_KIT_MENU = 2200;
    private static final int FORM_ADMIN_KIT_CREATE = 2201;
    private static final int FORM_ADMIN_KIT_DELETE = 2202;
    private static final int FORM_ADMIN_KIT_LIST = 2203;
    private static final int FORM_ADMIN_KIT_ITEM_KIT_SELECT = 2204;
    private static final int FORM_ADMIN_KIT_ITEM_MENU = 2205;
    private static final int FORM_ADMIN_KIT_ITEM_ADD = 2206;
    private static final int FORM_ADMIN_KIT_ITEM_REMOVE = 2207;
    private static final int FORM_ADMIN_KIT_ITEM_OVERVIEW = 2208;
    private static final int FORM_ADMIN_KIT_DELETE_CONFIRM = 2209;
    private static final int FORM_ADMIN_KIT_ARMOR_MENU = 2210;
    private static final int FORM_ADMIN_KIT_ARMOR_SET = 2211;
    private static final int FORM_ADMIN_KIT_ARMOR_REMOVE = 2212;

    private final ArenaCatalogService arenaCatalogService;
    private final ArenaSessionService arenaSessionService;
    private final SafeZonePlacementService safeZonePlacementService;
    private final KitRegistry kitRegistry;
    private final KitSelectionService kitSelectionService;
    private final KitApplicationService kitApplicationService;
    private final PlayerProfileService playerProfileService;
    private final FfaScoreboardService scoreboardService;
    private final LocalizationService localizationService;

    private final Map<UUID, String> pendingArenaStatusTarget = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingArenaDeleteTarget = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingKitDeleteTarget = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingKitEditTarget = new ConcurrentHashMap<>();
    private final Map<UUID, List<Integer>> pendingKitRemoveSlots = new ConcurrentHashMap<>();
    private final Map<UUID, List<PlayerMainAction>> playerMainActions = new ConcurrentHashMap<>();
    private final Map<UUID, List<PlayerStatsAction>> playerStatsActions = new ConcurrentHashMap<>();
    private volatile ArenaDefinition lastDeletedArena;
    private volatile KitDefinition lastDeletedKit;

    public FfaFormController(
            ArenaCatalogService arenaCatalogService,
            ArenaSessionService arenaSessionService,
            SafeZonePlacementService safeZonePlacementService,
            KitRegistry kitRegistry,
            KitSelectionService kitSelectionService,
            KitApplicationService kitApplicationService,
            PlayerProfileService playerProfileService,
            FfaScoreboardService scoreboardService,
            LocalizationService localizationService
    ) {
        this.arenaCatalogService = arenaCatalogService;
        this.arenaSessionService = arenaSessionService;
        this.safeZonePlacementService = safeZonePlacementService;
        this.kitRegistry = kitRegistry;
        this.kitSelectionService = kitSelectionService;
        this.kitApplicationService = kitApplicationService;
        this.playerProfileService = playerProfileService;
        this.scoreboardService = scoreboardService;
        this.localizationService = localizationService;
    }

    public void openPlayerMain(Player player) {
        boolean inArena = arenaSessionService.isPlayerInArena(player.getUniqueId());
        if (inArena) {
            openInArenaCommandMenu(player);
            return;
        }

        FormWindowSimple form = new FormWindowSimple(
                title(player, "FFA Control Center", "FFA Kontrollzentrum"),
                section(player,
                        "Choose a sector. Navigation stays strictly inside the selected flow.",
                        "Waehle einen Sektor. Die Navigation bleibt strikt im gewaehlten Ablauf.")
        );

        List<PlayerMainAction> actions = new ArrayList<>();
        form.addButton(new ElementButton(button(player, TextFormat.GOLD,
                "Arena",
                "Join, leave, and arena overview",
                "Arena",
                "Beitreten, verlassen, Arena-Uebersicht")));
        actions.add(PlayerMainAction.ARENA);

        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Stats",
                "View your combat statistics",
                "Stats",
                "Zeige deine Kampfstatistiken")));
        actions.add(PlayerMainAction.STATS);

        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Settings",
                "Language and scoreboard preferences",
                "Einstellungen",
                "Sprache und Scoreboard-Einstellungen")));
        actions.add(PlayerMainAction.SETTINGS);

        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Close", "Schliessen")));
        actions.add(PlayerMainAction.CLOSE);

        playerMainActions.put(player.getUniqueId(), actions);
        player.showFormWindow(form, FORM_PLAYER_MAIN);
    }

    private void openInArenaCommandMenu(Player player) {
        Optional<ArenaDefinition> arena = arenaSessionService.getArenaForPlayer(player.getUniqueId());
        String arenaName = arena.map(ArenaDefinition::getDisplayName).orElse("-");
        int shieldSeconds = arenaSessionService.getRespawnProtectionSeconds(player.getUniqueId());

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Arena Session", "Arena-Session"),
                TextFormat.GRAY + t(player,
                        "You are currently fighting in a live arena.",
                        "Du kaempfst aktuell in einer Live-Arena.")
                        + "\n" + TextFormat.DARK_GRAY + t(player, "Arena", "Arena") + ": " + TextFormat.WHITE + arenaName
                        + (shieldSeconds > 0
                        ? "\n" + TextFormat.DARK_GRAY + t(player, "Respawn Shield", "Respawn-Schutz") + ": "
                        + TextFormat.GREEN + shieldSeconds + "s"
                        : "")
        );
        form.addButton(new ElementButton(button(player, TextFormat.RED,
                "Leave Arena",
                "Exit combat and return to lobby",
                "Arena verlassen",
                "Kampf verlassen und zur Lobby gehen")));
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Kits",
                "Change your active combat kit",
                "Kits",
                "Aktives Kampf-Kit wechseln")));
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Settings",
                "Language and scoreboard preferences",
                "Einstellungen",
                "Sprache und Scoreboard-Einstellungen")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Close", "Schliessen")));
        player.showFormWindow(form, FORM_PLAYER_IN_ARENA_COMMAND);
    }

    public void openAdminMain(Player player) {
        FormWindowSimple form = new FormWindowSimple(
                title(player, "FFA Admin Console", "FFA Admin Konsole"),
                section(player,
                        "Arena and kit administration are strictly separated.",
                        "Arena- und Kit-Verwaltung sind strikt getrennt.")
        );
        form.addButton(new ElementButton(button(player, TextFormat.GOLD,
                "Arena Management",
                "Arenas, zones, status and safe-zone mode",
                "Arena Verwaltung",
                "Arenen, Zonen, Status und Safe-Zone Modus")));
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Kit Management",
                "Create, delete, inspect kits",
                "Kit Verwaltung",
                "Kits erstellen, loeschen, pruefen")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Close", "Schliessen")));
        player.showFormWindow(form, FORM_ADMIN_MAIN);
    }

    public void handleResponse(Player player, int formId, FormResponse response, boolean wasClosed) {
        if (wasClosed || response == null) {
            routeClosedForm(player, formId);
            return;
        }
        switch (formId) {
            case FORM_PLAYER_MAIN -> handlePlayerMain(player, response);
            case FORM_PLAYER_IN_ARENA_COMMAND -> handleInArenaCommandMenu(player, response);
            case FORM_PLAYER_ARENA_MENU -> handlePlayerArenaMenu(player, response);
            case FORM_PLAYER_ARENA_SELECT -> handlePlayerArenaSelect(player, response);
            case FORM_PLAYER_ARENA_OVERVIEW -> openPlayerArenaMenu(player);
            case FORM_PLAYER_KIT_MENU -> handlePlayerKitMenu(player, response);
            case FORM_PLAYER_KIT_SELECT -> handlePlayerKitSelect(player, response);
            case FORM_PLAYER_KIT_CURRENT -> openPlayerKitMenu(player);
            case FORM_PLAYER_STATS -> handlePlayerStatsMenu(player, response);
            case FORM_PLAYER_STATS_OVERVIEW, FORM_PLAYER_STATS_GLOBAL, FORM_PLAYER_STATS_ARENA -> openPlayerStats(player);
            case FORM_PLAYER_STATS_UNAVAILABLE -> openPlayerMain(player);
            case FORM_PLAYER_SETTINGS -> handlePlayerSettings(player, response);
            case FORM_PLAYER_ARENA_JOINED -> handleArenaJoinedForm(player, response);
            case FORM_ADMIN_MAIN -> handleAdminMain(player, response);
            case FORM_ADMIN_ARENA_MENU -> handleAdminArenaMenu(player, response);
            case FORM_ADMIN_ARENA_CREATE -> handleAdminArenaCreate(player, response);
            case FORM_ADMIN_ARENA_DELETE -> handleAdminArenaDelete(player, response);
            case FORM_ADMIN_ARENA_DELETE_CONFIRM -> handleAdminArenaDeleteConfirm(player, response);
            case FORM_ADMIN_ARENA_ADD_SPAWN -> handleAdminArenaAddSpawn(player, response);
            case FORM_ADMIN_ARENA_STATUS -> handleAdminArenaStatus(player, response);
            case FORM_ADMIN_ARENA_STATUS_VALUE -> handleAdminArenaStatusValue(player, response);
            case FORM_ADMIN_ZONE_MENU -> handleAdminZoneMenu(player, response);
            case FORM_ADMIN_ZONE_SAFE_MODE -> handleAdminSafeZoneMode(player, response);
            case FORM_ADMIN_ARENA_LIST -> openAdminArenaMenu(player);
            case FORM_ADMIN_KIT_MENU -> handleAdminKitMenu(player, response);
            case FORM_ADMIN_KIT_CREATE -> handleAdminKitCreate(player, response);
            case FORM_ADMIN_KIT_DELETE -> handleAdminKitDelete(player, response);
            case FORM_ADMIN_KIT_DELETE_CONFIRM -> handleAdminKitDeleteConfirm(player, response);
            case FORM_ADMIN_KIT_LIST -> openAdminKitMenu(player);
            case FORM_ADMIN_KIT_ITEM_KIT_SELECT -> handleAdminKitItemKitSelect(player, response);
            case FORM_ADMIN_KIT_ITEM_MENU -> handleAdminKitItemMenu(player, response);
            case FORM_ADMIN_KIT_ITEM_ADD -> handleAdminKitItemAdd(player, response);
            case FORM_ADMIN_KIT_ITEM_REMOVE -> handleAdminKitItemRemove(player, response);
            case FORM_ADMIN_KIT_ITEM_OVERVIEW -> openAdminKitItemMenu(player);
            case FORM_ADMIN_KIT_ARMOR_MENU -> handleAdminKitArmorMenu(player, response);
            case FORM_ADMIN_KIT_ARMOR_SET -> handleAdminKitArmorSet(player, response);
            case FORM_ADMIN_KIT_ARMOR_REMOVE -> handleAdminKitArmorRemove(player, response);
            default -> {
            }
        }
    }

    private void routeClosedForm(Player player, int formId) {
        switch (formId) {
            case FORM_PLAYER_ARENA_SELECT, FORM_PLAYER_ARENA_OVERVIEW, FORM_PLAYER_ARENA_JOINED -> openPlayerArenaMenu(player);
            case FORM_PLAYER_KIT_SELECT, FORM_PLAYER_KIT_CURRENT -> {
                if (arenaSessionService.isPlayerInArena(player.getUniqueId())) {
                    openPlayerKitMenu(player);
                } else {
                    openPlayerMain(player);
                }
            }
            case FORM_PLAYER_STATS_OVERVIEW, FORM_PLAYER_STATS_GLOBAL, FORM_PLAYER_STATS_ARENA -> openPlayerStats(player);
            case FORM_PLAYER_STATS_UNAVAILABLE -> openPlayerMain(player);
            case FORM_PLAYER_ARENA_MENU, FORM_PLAYER_KIT_MENU, FORM_PLAYER_STATS, FORM_PLAYER_SETTINGS -> openPlayerMain(player);
            case FORM_ADMIN_ARENA_CREATE, FORM_ADMIN_ARENA_DELETE, FORM_ADMIN_ARENA_ADD_SPAWN,
                    FORM_ADMIN_ARENA_STATUS, FORM_ADMIN_ZONE_MENU, FORM_ADMIN_ARENA_LIST -> openAdminArenaMenu(player);
            case FORM_ADMIN_ARENA_DELETE_CONFIRM -> openAdminArenaDelete(player);
            case FORM_ADMIN_ZONE_SAFE_MODE -> openAdminZoneMenu(player);
            case FORM_ADMIN_ARENA_STATUS_VALUE -> {
                pendingArenaStatusTarget.remove(player.getUniqueId());
                openAdminArenaStatus(player);
            }
            case FORM_ADMIN_KIT_CREATE, FORM_ADMIN_KIT_DELETE, FORM_ADMIN_KIT_LIST, FORM_ADMIN_KIT_ITEM_KIT_SELECT -> openAdminKitMenu(player);
            case FORM_ADMIN_KIT_DELETE_CONFIRM -> openAdminKitDelete(player);
            case FORM_ADMIN_KIT_ITEM_ADD, FORM_ADMIN_KIT_ITEM_OVERVIEW -> openAdminKitItemMenu(player);
            case FORM_ADMIN_KIT_ITEM_REMOVE -> {
                pendingKitRemoveSlots.remove(player.getUniqueId());
                openAdminKitItemMenu(player);
            }
            case FORM_ADMIN_KIT_ARMOR_MENU, FORM_ADMIN_KIT_ARMOR_SET, FORM_ADMIN_KIT_ARMOR_REMOVE -> openAdminKitItemMenu(player);
            case FORM_ADMIN_KIT_ITEM_MENU -> {
                pendingKitEditTarget.remove(player.getUniqueId());
                pendingKitRemoveSlots.remove(player.getUniqueId());
                openAdminKitMenu(player);
            }
            case FORM_ADMIN_ARENA_MENU, FORM_ADMIN_KIT_MENU -> openAdminMain(player);
            default -> {
            }
        }
    }

    public void clearPlayerState(UUID uniqueId) {
        pendingArenaStatusTarget.remove(uniqueId);
        pendingArenaDeleteTarget.remove(uniqueId);
        pendingKitDeleteTarget.remove(uniqueId);
        pendingKitEditTarget.remove(uniqueId);
        pendingKitRemoveSlots.remove(uniqueId);
        playerMainActions.remove(uniqueId);
        playerStatsActions.remove(uniqueId);
    }

    private void handlePlayerMain(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<PlayerMainAction> actions = playerMainActions.get(player.getUniqueId());
        if (actions == null || simple.getClickedButtonId() < 0 || simple.getClickedButtonId() >= actions.size()) {
            openPlayerMain(player);
            return;
        }
        PlayerMainAction action = actions.get(simple.getClickedButtonId());
        switch (action) {
            case ARENA -> openPlayerArenaMenu(player);
            case KITS -> openPlayerKitMenu(player);
            case STATS -> openPlayerStats(player);
            case SETTINGS -> openPlayerSettings(player);
            case CLOSE -> {
            }
        }
    }

    private void handleInArenaCommandMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> {
                arenaSessionService.leaveArena(player, true);
                scoreboardService.refreshPlayer(player);
                sendInfo(player, t(player, "Arena session ended.", "Arena-Session beendet."));
            }
            case 1 -> openPlayerKitMenu(player);
            case 2 -> openPlayerSettings(player);
            default -> {
            }
        }
    }

    private void openPlayerArenaMenu(Player player) {
        Optional<ArenaDefinition> arena = arenaSessionService.getArenaForPlayer(player.getUniqueId());
        String currentArena = arena.map(ArenaDefinition::getDisplayName).orElse(t(player, "No active arena", "Keine aktive Arena"));

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Arena Hub", "Arena-Hub"),
                TextFormat.GRAY + t(player, "Current Arena: ", "Aktuelle Arena: ")
                        + TextFormat.WHITE + currentArena
        );
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Join Arena",
                "Browse all available arenas",
                "Arena beitreten",
                "Alle verfuegbaren Arenen anzeigen")));
        form.addButton(new ElementButton(button(player, TextFormat.RED,
                "Leave Arena",
                "Exit your current arena session",
                "Arena verlassen",
                "Aktuelle Arena-Session verlassen")));
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Arena Overview",
                "Live status of all arenas",
                "Arena Uebersicht",
                "Live-Status aller Arenen")));
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_ARENA_MENU);
    }

    private void handlePlayerArenaMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> openPlayerArenaSelect(player);
            case 1 -> {
                if (!arenaSessionService.isPlayerInArena(player.getUniqueId())) {
                    sendWarning(player, t(player, "You are not inside an arena.", "Du bist aktuell in keiner Arena."));
                } else {
                    arenaSessionService.leaveArena(player, true);
                    scoreboardService.refreshPlayer(player);
                    sendInfo(player, t(player, "Arena session ended.", "Arena-Session beendet."));
                }
                openPlayerArenaMenu(player);
            }
            case 2 -> openPlayerArenaOverview(player);
            default -> openPlayerMain(player);
        }
    }

    private void openPlayerArenaSelect(Player player) {
        List<ArenaDefinition> arenas = sortedArenas();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Join Arena", "Arena beitreten"),
                section(player,
                        "Select one arena. Joining is only available while status is Live.",
                        "Waehle eine Arena. Beitritt ist nur moeglich, wenn der Status Live ist.")
        );

        if (arenas.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No arenas available", "Keine Arenen verfuegbar")));
        } else {
            for (ArenaDefinition arena : arenas) {
                int players = arenaSessionService.getArenaPlayerCount(arena.getArenaId());
                form.addButton(new ElementButton(TextFormat.YELLOW + arena.getDisplayName()
                        + "\n" + TextFormat.GRAY + arena.getWorldName()
                        + " | " + formatStatusBadge(player, arena.getStatus())
                        + TextFormat.GRAY + " | " + formatJoinBadge(player, arena.getStatus())
                        + " | " + players + " " + t(player, "players", "Spieler")));
            }
        }
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_ARENA_SELECT);
    }

    private void handlePlayerArenaSelect(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<ArenaDefinition> arenas = sortedArenas();
        int selected = simple.getClickedButtonId();
        int backIndex = arenas.isEmpty() ? 1 : arenas.size();
        if (selected < 0 || selected >= backIndex) {
            openPlayerArenaMenu(player);
            return;
        }
        if (arenas.isEmpty()) {
            openPlayerArenaMenu(player);
            return;
        }

        ArenaDefinition arena = arenas.get(selected);
        if (arena.getStatus() != ArenaStatus.ACTIVE) {
            sendWarning(player, t(player,
                    "You can only join arenas with status Live.",
                    "Du kannst nur Arenen mit Status Live betreten."));
            openPlayerArenaSelect(player);
            return;
        }
        if (!arenaSessionService.joinArena(player, arena.getArenaId())) {
            sendError(player, t(player,
                    "Could not join this arena. Check spawn setup and world loading.",
                    "Arena-Beitritt fehlgeschlagen. Pruefe Spawn-Setup und Welt."));
            openPlayerArenaSelect(player);
            return;
        }

        scoreboardService.refreshPlayer(player);
        sendSuccess(player, t(player,
                "Joined arena " + arena.getDisplayName() + ".",
                "Arena " + arena.getDisplayName() + " betreten."));
        openArenaJoinedForm(player, arena);
    }

    private void openArenaJoinedForm(Player player, ArenaDefinition arena) {
        boolean scoreboardEnabled = playerProfileService.isScoreboardEnabled(player.getUniqueId());
        String selectedKit = resolveKitName(player, kitSelectionService.resolveKitForPlayer(player.getUniqueId()));
        FormWindowCustom form = new FormWindowCustom(title(player, "Arena Session Ready", "Arena-Session bereit"));
        form.addElement(new ElementLabel(
                TextFormat.GREEN + t(player, "You are now fighting in", "Du kaempfst jetzt in")
                        + " " + TextFormat.YELLOW + arena.getDisplayName() + "\n"
                        + TextFormat.GRAY + t(player, "World", "Welt") + ": " + arena.getWorldName() + "\n"
                        + TextFormat.GRAY + t(player, "Kit", "Kit") + ": "
                        + (selectedKit == null ? t(player, "None selected", "Keins ausgewaehlt") : selectedKit)
        ));
        form.addElement(new ElementDropdown(
                t(player, "Quick Action", "Schnellaktion"),
                List.of(
                        t(player, "Arena Menu", "Arena-Menue"),
                        t(player, "Open Kits", "Kits oeffnen"),
                        t(player, "Open Stats", "Stats oeffnen"),
                        t(player, "Open Settings", "Einstellungen oeffnen")
                ),
                0
        ));
        form.addElement(new ElementToggle(t(player, "Enable scoreboard", "Scoreboard aktivieren"), scoreboardEnabled));
        player.showFormWindow(form, FORM_PLAYER_ARENA_JOINED);
    }

    private void handleArenaJoinedForm(Player player, FormResponse response) {
        if (!(response instanceof FormResponseCustom custom)) {
            return;
        }
        boolean scoreboardEnabled = custom.getToggleResponse(2);
        playerProfileService.setScoreboardEnabled(player.getUniqueId(), scoreboardEnabled);
        scoreboardService.refreshPlayer(player);

        int action = getDropdownIndex(custom, 1);
        switch (action) {
            case 1 -> openPlayerKitMenu(player);
            case 2 -> openPlayerStats(player);
            case 3 -> openPlayerSettings(player);
            default -> openPlayerArenaMenu(player);
        }
    }

    private void openPlayerArenaOverview(Player player) {
        List<ArenaDefinition> arenas = sortedArenas();
        StringBuilder content = new StringBuilder();
        if (arenas.isEmpty()) {
            content.append(TextFormat.GRAY).append(t(player, "No arenas configured.", "Keine Arenen konfiguriert."));
        } else {
            for (ArenaDefinition arena : arenas) {
                int players = arenaSessionService.getArenaPlayerCount(arena.getArenaId());
                boolean hasSafeZone = arena.getSafeZone().isPresent();
                content.append(TextFormat.YELLOW).append(arena.getDisplayName()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "World", "Welt")).append(": ")
                        .append(TextFormat.GRAY).append(arena.getWorldName()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Status", "Status")).append(": ")
                        .append(formatStatusBadge(player, arena.getStatus())).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Join", "Beitritt")).append(": ")
                        .append(formatJoinBadge(player, arena.getStatus())).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Readiness", "Bereitschaft")).append(": ")
                        .append(formatReadinessBadge(player, arena)).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Players", "Spieler")).append(": ")
                        .append(TextFormat.GRAY).append(players).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Spawns", "Spawns")).append(": ")
                        .append(TextFormat.GRAY).append(arena.getSpawnPoints().size()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Zones", "Zonen")).append(": ")
                        .append(TextFormat.GRAY).append(arena.getZones().size()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Safe-Zone", "Safe-Zone")).append(": ")
                        .append(hasSafeZone ? TextFormat.GREEN + t(player, "Configured", "Konfiguriert")
                                : TextFormat.RED + t(player, "Not configured", "Nicht konfiguriert"))
                        .append("\n\n");
            }
        }

        FormWindowSimple form = new FormWindowSimple(title(player, "Arena Overview", "Arena Uebersicht"), content.toString().trim());
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_ARENA_OVERVIEW);
    }

    private void openPlayerKitMenu(Player player) {
        if (!arenaSessionService.isPlayerInArena(player.getUniqueId())) {
            sendWarning(player, t(player, "Kits are available only inside an arena.", "Kits sind nur in einer Arena verfuegbar."));
            openPlayerArenaMenu(player);
            return;
        }

        String selectedKit = resolveKitName(player, kitSelectionService.resolveKitForPlayer(player.getUniqueId()));
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Kit Sector", "Kit Sektor"),
                TextFormat.GRAY + t(player, "Current kit: ", "Aktuelles Kit: ")
                        + TextFormat.WHITE + (selectedKit == null ? t(player, "None", "Keins") : selectedKit)
        );
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Select Kit",
                "Choose your active loadout",
                "Kit waehlen",
                "Aktives Loadout auswaehlen")));
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Current Kit Details",
                "Show your current selection",
                "Aktuelles Kit",
                "Zeige deine aktuelle Auswahl")));
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_KIT_MENU);
    }

    private void handlePlayerKitMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> openPlayerKitSelect(player);
            case 1 -> openPlayerCurrentKit(player);
            default -> openPlayerMain(player);
        }
    }

    private void openPlayerCurrentKit(Player player) {
        if (!arenaSessionService.isPlayerInArena(player.getUniqueId())) {
            sendWarning(player, t(player, "Kits are available only inside an arena.", "Kits sind nur in einer Arena verfuegbar."));
            openPlayerArenaMenu(player);
            return;
        }

        String selectedKit = resolveKitName(player, kitSelectionService.resolveKitForPlayer(player.getUniqueId()));
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Current Kit", "Aktuelles Kit"),
                TextFormat.GRAY + t(player, "Selected kit:", "Ausgewaehltes Kit:")
                        + "\n" + TextFormat.YELLOW + (selectedKit == null ? t(player, "No kit selected", "Kein Kit ausgewaehlt") : selectedKit)
        );
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_KIT_CURRENT);
    }

    private void openPlayerKitSelect(Player player) {
        if (!arenaSessionService.isPlayerInArena(player.getUniqueId())) {
            sendWarning(player, t(player, "Kits are available only inside an arena.", "Kits sind nur in einer Arena verfuegbar."));
            openPlayerArenaMenu(player);
            return;
        }

        List<KitDefinition> kits = sortedKits();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Select Kit", "Kit Auswahl"),
                section(player,
                        "Choose one kit. It will be applied immediately in the arena.",
                        "Waehle ein Kit. Es wird direkt in der Arena angewendet.")
        );
        if (kits.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No kits available", "Keine Kits verfuegbar")));
        } else {
            for (KitDefinition kit : kits) {
                form.addButton(new ElementButton(TextFormat.YELLOW + kit.getDisplayName()));
            }
        }
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_KIT_SELECT);
    }

    private void handlePlayerKitSelect(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<KitDefinition> kits = sortedKits();
        int selected = simple.getClickedButtonId();
        int backIndex = kits.isEmpty() ? 1 : kits.size();
        if (selected < 0 || selected >= backIndex) {
            openPlayerKitMenu(player);
            return;
        }
        if (kits.isEmpty()) {
            openPlayerKitMenu(player);
            return;
        }

        KitDefinition kit = kits.get(selected);
        if (!kitSelectionService.selectKit(player.getUniqueId(), kit.getKitId())) {
            sendError(player, t(player, "Kit could not be selected.", "Kit konnte nicht gesetzt werden."));
            openPlayerKitSelect(player);
            return;
        }
        kitApplicationService.applyKit(player, kit.getKitId());
        sendSuccess(player, t(player, "Kit activated: ", "Kit aktiviert: ") + kit.getDisplayName());
        openPlayerKitMenu(player);
    }

    private void openPlayerStats(Player player) {
        if (!playerProfileService.isDataAvailable()) {
            openStatsUnavailable(player);
            return;
        }

        boolean inArena = arenaSessionService.isPlayerInArena(player.getUniqueId());
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Statistics", "Statistiken"),
                section(player,
                        "Open your personal profile and competitive leaderboards.",
                        "Oeffne dein persoenliches Profil und kompetitive Leaderboards.")
        );

        List<PlayerStatsAction> actions = new ArrayList<>();
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Personal Stats",
                "Your own combat profile",
                "Persoenliche Stats",
                "Dein eigenes Kampfprofil")));
        actions.add(PlayerStatsAction.PERSONAL);

        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Global Leaderboard",
                "Top players from stored database stats",
                "Globales Leaderboard",
                "Top-Spieler aus gespeicherten Daten")));
        actions.add(PlayerStatsAction.GLOBAL);

        if (inArena) {
            form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                    "Arena Leaderboard",
                    "Top players currently in your arena",
                    "Arena-Leaderboard",
                    "Top-Spieler aktuell in deiner Arena")));
            actions.add(PlayerStatsAction.ARENA);
        }

        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        actions.add(PlayerStatsAction.BACK);
        playerStatsActions.put(player.getUniqueId(), actions);
        player.showFormWindow(form, FORM_PLAYER_STATS);
    }

    private void handlePlayerStatsMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<PlayerStatsAction> actions = playerStatsActions.get(player.getUniqueId());
        if (actions == null || simple.getClickedButtonId() < 0 || simple.getClickedButtonId() >= actions.size()) {
            openPlayerMain(player);
            return;
        }
        PlayerStatsAction action = actions.get(simple.getClickedButtonId());
        switch (action) {
            case PERSONAL -> openPlayerStatsOverview(player);
            case GLOBAL -> openPlayerGlobalLeaderboard(player);
            case ARENA -> openPlayerArenaLeaderboard(player);
            case BACK -> openPlayerMain(player);
        }
    }

    private void openPlayerStatsOverview(Player player) {
        if (!playerProfileService.isDataAvailable()) {
            openStatsUnavailable(player);
            return;
        }

        PlayerStatistics statistics = playerProfileService.getStatisticsOrDefault(player.getUniqueId());
        double kd = statistics.getDeaths() == 0 ? statistics.getKills() : (double) statistics.getKills() / statistics.getDeaths();
        String kdText = String.format(Locale.ROOT, "%.2f", kd);
        String content = TextFormat.GRAY + t(player, "Combat Statistics", "Kampfstatistiken") + "\n"
                + TextFormat.DARK_GRAY + t(player, "Kills: ", "Kills: ") + TextFormat.GREEN + statistics.getKills() + "\n"
                + TextFormat.DARK_GRAY + t(player, "Deaths: ", "Tode: ") + TextFormat.RED + statistics.getDeaths() + "\n"
                + TextFormat.DARK_GRAY + t(player, "Killstreak: ", "Killstreak: ") + TextFormat.AQUA + statistics.getCurrentKillstreak() + "\n"
                + TextFormat.DARK_GRAY + t(player, "Best streak: ", "Beste Streak: ") + TextFormat.YELLOW + statistics.getBestKillstreak() + "\n"
                + TextFormat.DARK_GRAY + "K/D: " + TextFormat.WHITE + kdText;
        FormWindowSimple form = new FormWindowSimple(title(player, "Personal Stats", "Persoenliche Stats"), content);
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_STATS_OVERVIEW);
    }

    private void openPlayerGlobalLeaderboard(Player player) {
        if (!playerProfileService.isDataAvailable()) {
            openStatsUnavailable(player);
            return;
        }

        List<PlayerProfile> profiles = playerProfileService.loadAllProfilesSnapshot();
        StringBuilder content = new StringBuilder();

        appendLeaderboardSection(
                content,
                t(player, "Top Kills", "Top Kills"),
                profiles.stream()
                        .sorted(Comparator.comparingInt((PlayerProfile profile) -> profile.getStatistics().getKills()).reversed())
                        .limit(10)
                        .toList(),
                profile -> profile.getStatistics().getKills()
        );
        appendLeaderboardSection(
                content,
                t(player, "Top Streak", "Top Streak"),
                profiles.stream()
                        .sorted(Comparator.comparingInt((PlayerProfile profile) -> profile.getStatistics().getBestKillstreak()).reversed())
                        .limit(10)
                        .toList(),
                profile -> profile.getStatistics().getBestKillstreak()
        );
        appendLeaderboardSection(
                content,
                t(player, "Top K/D", "Top K/D"),
                profiles.stream()
                        .sorted(Comparator.comparingDouble((PlayerProfile profile) -> kdOf(profile.getStatistics())).reversed())
                        .limit(10)
                        .toList(),
                profile -> String.format(Locale.ROOT, "%.2f", kdOf(profile.getStatistics()))
        );

        if (content.isEmpty()) {
            content.append(TextFormat.GRAY).append(t(player, "No leaderboard data available.", "Keine Leaderboard-Daten verfuegbar."));
        }

        FormWindowSimple form = new FormWindowSimple(title(player, "Global Leaderboard", "Globales Leaderboard"), content.toString().trim());
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_STATS_GLOBAL);
    }

    private void openPlayerArenaLeaderboard(Player player) {
        if (!playerProfileService.isDataAvailable()) {
            openStatsUnavailable(player);
            return;
        }

        Optional<String> arenaIdOptional = arenaSessionService.getArenaIdForPlayer(player.getUniqueId());
        if (arenaIdOptional.isEmpty()) {
            sendWarning(player, t(player, "You are not inside an arena.", "Du bist aktuell in keiner Arena."));
            openPlayerStats(player);
            return;
        }

        String arenaId = arenaIdOptional.get();
        String arenaName = resolveArenaName(player, arenaId);
        List<UUID> arenaPlayers = new ArrayList<>(arenaSessionService.getPlayersInArena(arenaId));
        List<PlayerProfile> snapshots = playerProfileService.loadAllProfilesSnapshot();
        Map<UUID, PlayerProfile> profilesById = new ConcurrentHashMap<>();
        for (PlayerProfile snapshot : snapshots) {
            profilesById.put(snapshot.getUniqueId(), snapshot);
        }

        List<PlayerProfile> arenaProfiles = arenaPlayers.stream()
                .map(profilesById::get)
                .filter(profile -> profile != null)
                .toList();

        StringBuilder content = new StringBuilder();
        appendLeaderboardSection(
                content,
                t(player, "Arena Kills", "Arena-Kills"),
                arenaProfiles.stream()
                        .sorted(Comparator.comparingInt((PlayerProfile profile) -> profile.getStatistics().getKills()).reversed())
                        .limit(10)
                        .toList(),
                profile -> profile.getStatistics().getKills()
        );
        appendLeaderboardSection(
                content,
                t(player, "Arena Streak", "Arena-Streak"),
                arenaProfiles.stream()
                        .sorted(Comparator.comparingInt((PlayerProfile profile) -> profile.getStatistics().getCurrentKillstreak()).reversed())
                        .limit(10)
                        .toList(),
                profile -> profile.getStatistics().getCurrentKillstreak()
        );

        if (content.isEmpty()) {
            content.append(TextFormat.GRAY).append(t(player, "No arena leaderboard data available.", "Keine Arena-Leaderboard-Daten verfuegbar."));
        }

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Arena Leaderboard", "Arena-Leaderboard"),
                TextFormat.DARK_GRAY + t(player, "Arena", "Arena") + ": " + TextFormat.WHITE + arenaName + "\n\n" + content.toString().trim()
        );
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_STATS_ARENA);
    }

    private void openStatsUnavailable(Player player) {
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Statistics Unavailable", "Statistiken nicht verfuegbar"),
                section(player,
                        "The statistics service is currently unavailable because the database connection was lost.",
                        "Der Statistikdienst ist aktuell nicht verfuegbar, weil die Datenbankverbindung verloren wurde.")
                        + "\n\n" + TextFormat.GRAY + t(player,
                        "Try again later or ask an administrator to check database connectivity.",
                        "Versuche es spaeter erneut oder informiere einen Administrator zur Datenbankpruefung.")
        );
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_STATS_UNAVAILABLE);
    }

    private void appendLeaderboardSection(
            StringBuilder builder,
            String title,
            List<PlayerProfile> entries,
            java.util.function.Function<PlayerProfile, Object> valueExtractor
    ) {
        if (entries.isEmpty()) {
            return;
        }
        builder.append(TextFormat.GOLD).append(title).append("\n");
        int rank = 1;
        for (PlayerProfile entry : entries) {
            builder.append(TextFormat.YELLOW).append("#").append(rank).append(" ")
                    .append(TextFormat.WHITE).append(entry.getLastKnownName())
                    .append(TextFormat.GRAY).append(" -> ")
                    .append(TextFormat.AQUA).append(valueExtractor.apply(entry))
                    .append("\n");
            rank++;
        }
        builder.append("\n");
    }

    private double kdOf(PlayerStatistics statistics) {
        if (statistics.getDeaths() == 0) {
            return statistics.getKills();
        }
        return (double) statistics.getKills() / statistics.getDeaths();
    }

    private void openPlayerSettings(Player player) {
        boolean scoreboardEnabled = playerProfileService.isScoreboardEnabled(player.getUniqueId());
        PlayerLanguage language = playerProfileService.getLanguage(player.getUniqueId());
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Settings", "Einstellungen"),
                TextFormat.GRAY + t(player, "Language", "Sprache") + ": "
                        + TextFormat.WHITE + (language == PlayerLanguage.GERMAN ? "Deutsch" : "English") + "\n"
                        + TextFormat.GRAY + t(player, "Scoreboard", "Scoreboard") + ": "
                        + (scoreboardEnabled ? TextFormat.GREEN + t(player, "Enabled", "Aktiv")
                        : TextFormat.RED + t(player, "Disabled", "Deaktiviert"))
        );
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Toggle Scoreboard",
                "Enable or disable arena scoreboard",
                "Scoreboard umschalten",
                "Arena-Scoreboard aktivieren oder deaktivieren")));
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Switch Language",
                "English <-> Deutsch",
                "Sprache wechseln",
                "English <-> Deutsch")));
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_PLAYER_SETTINGS);
    }

    private void handlePlayerSettings(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        if (!playerProfileService.isDataAvailable()) {
            sendWarning(player, t(player,
                    "Settings are temporarily unavailable while database connectivity is down.",
                    "Einstellungen sind voruebergehend nicht verfuegbar, solange die Datenbankverbindung ausfaellt."));
            openPlayerMain(player);
            return;
        }
        if (simple.getClickedButtonId() == 0) {
            boolean enabled = playerProfileService.toggleScoreboard(player.getUniqueId()).orElse(true);
            scoreboardService.refreshPlayer(player);
            sendInfo(player, enabled
                    ? t(player, "Scoreboard enabled.", "Scoreboard aktiviert.")
                    : t(player, "Scoreboard disabled.", "Scoreboard deaktiviert."));
            openPlayerSettings(player);
            return;
        }
        if (simple.getClickedButtonId() == 1) {
            PlayerLanguage current = playerProfileService.getLanguage(player.getUniqueId());
            PlayerLanguage next = current == PlayerLanguage.GERMAN ? PlayerLanguage.ENGLISH : PlayerLanguage.GERMAN;
            playerProfileService.setLanguage(player.getUniqueId(), next);
            sendSuccess(player, next == PlayerLanguage.GERMAN
                    ? "Sprache auf Deutsch gesetzt."
                    : "Language switched to English.");
            openPlayerSettings(player);
            return;
        }
        openPlayerMain(player);
    }

    private void handleAdminMain(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> openAdminArenaMenu(player);
            case 1 -> openAdminKitMenu(player);
            default -> {
            }
        }
    }

    private void openAdminArenaMenu(Player player) {
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Arena Management", "Arena Verwaltung"),
                section(player,
                        "Arena tools only. No kit controls are mixed in.",
                        "Nur Arena-Werkzeuge. Keine vermischten Kit-Funktionen.")
        );
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Create Arena",
                "Create a new arena definition",
                "Arena erstellen",
                "Neue Arena-Definition erstellen")));
        form.addButton(new ElementButton(button(player, TextFormat.RED,
                "Delete Arena",
                "Remove an existing arena",
                "Arena loeschen",
                "Bestehende Arena entfernen")));
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Add Spawn",
                "Store your current location as spawn",
                "Spawn hinzufuegen",
                "Aktuelle Position als Spawn speichern")));
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Set Arena Status",
                "Standby, live, offline, maintenance",
                "Arena-Status setzen",
                "Standby, live, offline, wartung")));
        form.addButton(new ElementButton(button(player, TextFormat.GOLD,
                "Zone Tools",
                "Custom zones and safe-zone placement mode",
                "Zone Tools",
                "Eigene Zonen und Safe-Zone Modus")));
        form.addButton(new ElementButton(button(player, TextFormat.WHITE,
                "Arena Overview",
                "Inspect all arena technical details",
                "Arena Uebersicht",
                "Alle Arena-Details anzeigen")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ARENA_MENU);
    }

    private void handleAdminArenaMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> openAdminArenaCreate(player);
            case 1 -> openAdminArenaDelete(player);
            case 2 -> openAdminArenaAddSpawn(player);
            case 3 -> openAdminArenaStatus(player);
            case 4 -> openAdminZoneMenu(player);
            case 5 -> openAdminArenaList(player);
            default -> openAdminMain(player);
        }
    }

    private void openAdminArenaCreate(Player player) {
        FormWindowCustom form = new FormWindowCustom(title(player, "Create Arena", "Arena erstellen"));
        form.addElement(new ElementInput(t(player, "Arena ID", "Arena ID"), "city"));
        form.addElement(new ElementInput(t(player, "Display Name (optional)", "Anzeigename (optional)"), "City Arena"));
        form.addElement(new ElementInput(t(player, "World Name", "Weltname"), player.getLevel().getName()));
        form.addElement(new ElementDropdown(
                t(player, "Action", "Aktion"),
                List.of(t(player, "Create Arena", "Arena erstellen"), t(player, "Back", "Zurueck")),
                0
        ));
        player.showFormWindow(form, FORM_ADMIN_ARENA_CREATE);
    }

    private void handleAdminArenaCreate(Player player, FormResponse response) {
        if (!(response instanceof FormResponseCustom custom)) {
            return;
        }
        int action = getDropdownIndex(custom, 3);
        if (action == 1) {
            openAdminArenaMenu(player);
            return;
        }

        String arenaId = normalizeIdentifier(custom.getInputResponse(0));
        String displayName = trimInput(custom.getInputResponse(1));
        String worldName = trimInput(custom.getInputResponse(2));
        if (arenaId == null || worldName == null) {
            sendError(player, t(player, "Arena ID and world are required.", "Arena ID und Welt sind Pflicht."));
            openAdminArenaCreate(player);
            return;
        }
        boolean created = arenaCatalogService.createArena(arenaId, displayName, worldName);
        String arenaName = displayName == null ? arenaId : displayName;
        if (created) {
            sendSuccess(player, t(player, "Arena created: ", "Arena erstellt: ") + arenaName);
        } else {
            sendError(player, t(player, "Arena could not be created.", "Arena konnte nicht erstellt werden."));
        }
        openAdminArenaMenu(player);
    }

    private void openAdminArenaDelete(Player player) {
        List<ArenaDefinition> arenas = sortedArenas();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Delete Arena", "Arena loeschen"),
                section(player, "Select an arena to delete.", "Waehle eine Arena zum Loeschen.")
        );
        if (arenas.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No arenas available", "Keine Arenen verfuegbar")));
        } else {
            for (ArenaDefinition arena : arenas) {
                form.addButton(new ElementButton(TextFormat.YELLOW + arena.getDisplayName() + "\n"
                        + TextFormat.GRAY + arena.getWorldName()));
            }
        }
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ARENA_DELETE);
    }

    private void handleAdminArenaDelete(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<ArenaDefinition> arenas = sortedArenas();
        int selected = simple.getClickedButtonId();
        int backIndex = arenas.isEmpty() ? 1 : arenas.size();
        if (selected < 0 || selected >= backIndex) {
            openAdminArenaMenu(player);
            return;
        }
        if (arenas.isEmpty()) {
            openAdminArenaMenu(player);
            return;
        }
        ArenaDefinition selectedArena = arenas.get(selected);
        pendingArenaDeleteTarget.put(player.getUniqueId(), selectedArena.getArenaId());
        openAdminArenaDeleteConfirm(player, selectedArena.getDisplayName());
    }

    private void openAdminArenaDeleteConfirm(Player player, String arenaName) {
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Confirm Arena Deletion", "Arena-Loeschung bestaetigen"),
                TextFormat.RED + t(player, "This will permanently delete arena ", "Diese Aktion loescht die Arena dauerhaft ")
                        + TextFormat.YELLOW + arenaName + TextFormat.RED + "."
        );
        form.addButton(new ElementButton(TextFormat.RED + t(player, "Delete Now", "Jetzt loeschen")));
        form.addButton(new ElementButton(TextFormat.YELLOW + t(player, "Undo Last Delete", "Letzte Loeschung rueckgaengig")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ARENA_DELETE_CONFIRM);
    }

    private void handleAdminArenaDeleteConfirm(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        if (simple.getClickedButtonId() == 1) {
            if (lastDeletedArena != null && arenaCatalogService.restoreArena(lastDeletedArena)) {
                sendSuccess(player, t(player, "Last deleted arena restored.", "Letzte geloeschte Arena wiederhergestellt."));
            } else {
                sendWarning(player, t(player, "No deletable arena in undo buffer.", "Kein Arena-Eintrag im Undo-Puffer."));
            }
            openAdminArenaDelete(player);
            return;
        }
        if (simple.getClickedButtonId() != 0) {
            pendingArenaDeleteTarget.remove(player.getUniqueId());
            openAdminArenaDelete(player);
            return;
        }

        String arenaId = pendingArenaDeleteTarget.remove(player.getUniqueId());
        if (arenaId == null) {
            openAdminArenaDelete(player);
            return;
        }

        Optional<ArenaDefinition> removed = arenaCatalogService.removeArena(arenaId);
        if (removed.isPresent()) {
            lastDeletedArena = removed.get();
            sendSuccess(player, t(player, "Arena deleted. Undo is available.", "Arena geloescht. Undo ist verfuegbar."));
        } else {
            sendError(player, t(player, "Arena deletion failed.", "Arena konnte nicht geloescht werden."));
        }
        openAdminArenaDelete(player);
    }

    private void openAdminArenaAddSpawn(Player player) {
        List<ArenaDefinition> arenas = sortedArenas();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Add Spawn", "Spawn hinzufuegen"),
                section(player,
                        "Select one arena. Your current location becomes a spawn point.",
                        "Waehle eine Arena. Deine aktuelle Position wird als Spawn gespeichert.")
        );
        if (arenas.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No arenas available", "Keine Arenen verfuegbar")));
        } else {
            for (ArenaDefinition arena : arenas) {
                form.addButton(new ElementButton(TextFormat.YELLOW + arena.getDisplayName() + "\n"
                        + TextFormat.GRAY + arena.getWorldName()));
            }
        }
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ARENA_ADD_SPAWN);
    }

    private void handleAdminArenaAddSpawn(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<ArenaDefinition> arenas = sortedArenas();
        int selected = simple.getClickedButtonId();
        int backIndex = arenas.isEmpty() ? 1 : arenas.size();
        if (selected < 0 || selected >= backIndex) {
            openAdminArenaMenu(player);
            return;
        }
        if (arenas.isEmpty()) {
            openAdminArenaMenu(player);
            return;
        }
        boolean added = arenaCatalogService.addSpawnPoint(arenas.get(selected).getArenaId(), player.getLocation());
        if (added) {
            sendSuccess(player, t(player, "Spawn saved.", "Spawn gespeichert."));
        } else {
            sendError(player, t(player, "Spawn could not be saved.", "Spawn konnte nicht gespeichert werden."));
        }
        openAdminArenaAddSpawn(player);
    }

    private void openAdminArenaStatus(Player player) {
        List<ArenaDefinition> arenas = sortedArenas();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Set Arena Status", "Arena-Status setzen"),
                section(player, "Select one arena. Players can join only Live arenas.", "Waehle eine Arena. Spieler koennen nur Live-Arenen betreten.")
        );
        if (arenas.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No arenas available", "Keine Arenen verfuegbar")));
        } else {
            for (ArenaDefinition arena : arenas) {
                form.addButton(new ElementButton(TextFormat.YELLOW + arena.getDisplayName() + "\n"
                        + formatStatusBadge(player, arena.getStatus())
                        + TextFormat.GRAY + " | " + formatJoinBadge(player, arena.getStatus()) + "\n"
                        + formatReadinessBadge(player, arena)));
            }
        }
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ARENA_STATUS);
    }

    private void handleAdminArenaStatus(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<ArenaDefinition> arenas = sortedArenas();
        int selected = simple.getClickedButtonId();
        int backIndex = arenas.isEmpty() ? 1 : arenas.size();
        if (selected < 0 || selected >= backIndex) {
            openAdminArenaMenu(player);
            return;
        }
        if (arenas.isEmpty()) {
            openAdminArenaMenu(player);
            return;
        }

        ArenaDefinition selectedArena = arenas.get(selected);
        String arenaId = selectedArena.getArenaId();
        pendingArenaStatusTarget.put(player.getUniqueId(), arenaId);

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Select Status", "Status auswaehlen"),
                TextFormat.GRAY + t(player, "Arena", "Arena") + ": " + TextFormat.YELLOW + selectedArena.getDisplayName()
        );
        for (ArenaStatus status : ArenaStatus.values()) {
            form.addButton(new ElementButton(formatStatusBadge(player, status) + "\n"
                    + TextFormat.GRAY + formatJoinBadge(player, status)));
        }
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ARENA_STATUS_VALUE);
    }

    private void handleAdminArenaStatusValue(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        int selected = simple.getClickedButtonId();
        int backIndex = ArenaStatus.values().length;
        if (selected < 0 || selected >= backIndex) {
            pendingArenaStatusTarget.remove(player.getUniqueId());
            openAdminArenaStatus(player);
            return;
        }

        String arenaId = pendingArenaStatusTarget.remove(player.getUniqueId());
        if (arenaId == null) {
            openAdminArenaStatus(player);
            return;
        }

        ArenaStatus status = ArenaStatus.values()[selected];
        boolean updated = arenaCatalogService.setArenaStatus(arenaId, status);
        if (updated) {
            sendSuccess(player, t(player, "Status updated to ", "Status aktualisiert auf ") + formatStatus(player, status) + ".");
        } else {
            if (status == ArenaStatus.ACTIVE) {
                String reason = arenaCatalogService.validateActivationReadiness(arenaId)
                        .map(error -> localizeReadinessReason(player, error))
                        .orElse(t(player, "Unknown readiness error.", "Unbekannter Bereitschaftsfehler."));
                sendError(player, t(player, "Arena is not ready for Live: ", "Arena ist nicht bereit fuer Live: ") + reason);
            } else {
                sendError(player, t(player, "Status update failed.", "Status konnte nicht gesetzt werden."));
            }
        }
        openAdminArenaStatus(player);
    }

    public void openAdminZoneMenu(Player player) {
        boolean safeModeActive = safeZonePlacementService.isPlacing(player.getUniqueId());
        String safeModeState = safeModeActive ? TextFormat.GREEN + t(player, "ACTIVE", "AKTIV")
                : TextFormat.RED + t(player, "INACTIVE", "INAKTIV");

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Zone Tools", "Zone Tools"),
                TextFormat.GRAY + t(player,
                "Safe-Zone setup uses 2-click mode and saves automatically.",
                "Safe-Zone Setup nutzt den 2-Klick-Modus und speichert automatisch.")
                        + "\n" + TextFormat.DARK_GRAY + t(player, "Mode: ", "Modus: ") + safeModeState
        );
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Start Safe-Zone 2-Click Mode",
                "Select arena and click two blocks in world",
                "Safe-Zone 2-Klick Modus starten",
                "Arena waehlen und zwei Bloecke anklicken")));
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Cancel Safe-Zone Mode",
                "Disable current click-mode session",
                "Safe-Zone Modus abbrechen",
                "Aktive Click-Mode Session beenden")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ZONE_MENU);
    }

    private void handleAdminZoneMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> openAdminSafeZoneMode(player);
            case 1 -> {
                safeZonePlacementService.cancel(player.getUniqueId());
                sendInfo(player, t(player, "Safe-Zone mode cancelled.", "Safe-Zone Modus abgebrochen."));
                openAdminZoneMenu(player);
            }
            default -> openAdminArenaMenu(player);
        }
    }

    private void openAdminSafeZoneMode(Player player) {
        List<ArenaDefinition> arenas = sortedArenas();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Safe-Zone 2-Click Mode", "Safe-Zone 2-Klick Modus"),
                section(player,
                        "Select an arena. Then click two blocks in the arena world.",
                        "Waehle eine Arena. Danach klickst du zwei Bloecke in der Arena-Welt.")
        );
        if (arenas.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No arenas available", "Keine Arenen verfuegbar")));
        } else {
            for (ArenaDefinition arena : arenas) {
                form.addButton(new ElementButton(TextFormat.YELLOW + arena.getDisplayName() + "\n"
                        + TextFormat.GRAY + arena.getWorldName()));
            }
        }
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ZONE_SAFE_MODE);
    }

    private void handleAdminSafeZoneMode(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<ArenaDefinition> arenas = sortedArenas();
        int selected = simple.getClickedButtonId();
        int backIndex = arenas.isEmpty() ? 1 : arenas.size();
        if (selected < 0 || selected >= backIndex) {
            openAdminZoneMenu(player);
            return;
        }
        if (arenas.isEmpty()) {
            openAdminZoneMenu(player);
            return;
        }

        ArenaDefinition selectedArena = arenas.get(selected);
        String arenaId = selectedArena.getArenaId();
        if (!safeZonePlacementService.beginPlacement(player, arenaId)) {
            sendError(player, t(player, "Safe-Zone mode could not start.", "Safe-Zone Modus konnte nicht gestartet werden."));
            openAdminZoneMenu(player);
        } else {
            sendSuccess(player, t(player,
                    "Safe-Zone mode started for arena " + selectedArena.getDisplayName() + ".",
                    "Safe-Zone Modus fuer Arena " + selectedArena.getDisplayName() + " gestartet."));
        }
    }

    private void openAdminArenaList(Player player) {
        List<ArenaDefinition> arenas = sortedArenas();
        StringBuilder content = new StringBuilder();
        if (arenas.isEmpty()) {
            content.append(TextFormat.GRAY).append(t(player, "No arenas available.", "Keine Arenen verfuegbar."));
        } else {
            for (ArenaDefinition arena : arenas) {
                content.append(TextFormat.YELLOW).append(arena.getDisplayName()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "World", "Welt"))
                        .append(": ").append(TextFormat.GRAY).append(arena.getWorldName()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Status", "Status"))
                        .append(": ").append(formatStatusBadge(player, arena.getStatus())).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Join", "Beitritt"))
                        .append(": ").append(formatJoinBadge(player, arena.getStatus())).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Readiness", "Bereitschaft"))
                        .append(": ").append(formatReadinessBadge(player, arena)).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Spawn Points", "Spawnpunkte"))
                        .append(": ").append(TextFormat.GRAY).append(arena.getSpawnPoints().size()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Zones", "Zonen"))
                        .append(": ").append(TextFormat.GRAY).append(arena.getZones().size()).append("\n")
                        .append(TextFormat.DARK_GRAY).append(" - ").append(t(player, "Safe-Zone", "Safe-Zone"))
                        .append(": ").append(arena.getSafeZone().isPresent()
                                ? TextFormat.GREEN + t(player, "Configured", "Konfiguriert")
                                : TextFormat.RED + t(player, "None", "Keine"))
                        .append("\n\n");
            }
        }
        FormWindowSimple form = new FormWindowSimple(title(player, "Arena Overview", "Arena Uebersicht"), content.toString().trim());
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_ARENA_LIST);
    }

    private void openAdminKitMenu(Player player) {
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Kit Management", "Kit Verwaltung"),
                section(player,
                        "Kit tools only. No arena controls are mixed in.",
                        "Nur Kit-Werkzeuge. Keine vermischten Arena-Funktionen.")
        );
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Create / Update Kit",
                "Save your current inventory and armor as kit",
                "Kit erstellen / aktualisieren",
                "Aktuelles Inventar und Ruestung als Kit speichern")));
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Manage Kit Items",
                "Add, replace, or remove individual items",
                "Kit-Items verwalten",
                "Einzelne Items hinzufuegen, ersetzen oder entfernen")));
        form.addButton(new ElementButton(button(player, TextFormat.RED,
                "Delete Kit",
                "Remove an existing kit",
                "Kit loeschen",
                "Bestehendes Kit entfernen")));
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Kit List",
                "Show all registered kits",
                "Kit Liste",
                "Alle registrierten Kits anzeigen")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_MENU);
    }

    private void handleAdminKitMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> openAdminKitCreate(player);
            case 1 -> openAdminKitItemKitSelect(player);
            case 2 -> openAdminKitDelete(player);
            case 3 -> openAdminKitList(player);
            default -> openAdminMain(player);
        }
    }

    private void openAdminKitCreate(Player player) {
        FormWindowCustom form = new FormWindowCustom(title(player, "Create / Update Kit", "Kit erstellen / aktualisieren"));
        form.addElement(new ElementInput(t(player, "Kit ID", "Kit ID"), "archer"));
        form.addElement(new ElementInput(t(player, "Display Name", "Anzeigename"), "Archer"));
        form.addElement(new ElementDropdown(
                t(player, "Save Mode", "Speichermodus"),
                List.of(
                        t(player, "Create/Rename only", "Nur erstellen/umbenennen"),
                        t(player, "Snapshot from inventory", "Snapshot aus Inventar"),
                        t(player, "Back", "Zurueck")
                ),
                0
        ));
        player.showFormWindow(form, FORM_ADMIN_KIT_CREATE);
    }

    private void handleAdminKitCreate(Player player, FormResponse response) {
        if (!(response instanceof FormResponseCustom custom)) {
            return;
        }
        int action = getDropdownIndex(custom, 2);
        if (action == 2) {
            openAdminKitMenu(player);
            return;
        }

        String kitId = normalizeIdentifier(custom.getInputResponse(0));
        String displayName = trimInput(custom.getInputResponse(1));
        if (kitId == null) {
            sendError(player, t(player, "Kit ID is required.", "Kit ID ist Pflicht."));
            openAdminKitCreate(player);
            return;
        }

        boolean created;
        if (action == 0) {
            created = kitRegistry.createOrRenameKit(kitId, displayName == null ? kitId : displayName);
        } else {
            created = kitRegistry.createOrUpdateKitFromInventory(
                    kitId,
                    displayName == null ? kitId : displayName,
                    player
            );
        }
        if (created) {
            sendSuccess(player, action == 0
                    ? t(player, "Kit metadata saved.", "Kit-Metadaten gespeichert.")
                    : t(player, "Kit snapshot saved from inventory.", "Kit-Snapshot aus Inventar gespeichert."));
        } else {
            sendError(player, t(player, "Kit could not be saved.", "Kit konnte nicht gespeichert werden."));
        }
        openAdminKitMenu(player);
    }

    private void openAdminKitItemKitSelect(Player player) {
        List<KitDefinition> kits = sortedKits();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Select Kit", "Kit auswaehlen"),
                section(player,
                        "Choose a kit to manage individual items.",
                        "Waehle ein Kit, um einzelne Items zu verwalten.")
        );
        if (kits.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No kits available", "Keine Kits verfuegbar")));
        } else {
            for (KitDefinition kit : kits) {
                form.addButton(new ElementButton(TextFormat.YELLOW + kit.getDisplayName()));
            }
        }
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_ITEM_KIT_SELECT);
    }

    private void handleAdminKitItemKitSelect(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<KitDefinition> kits = sortedKits();
        int selected = simple.getClickedButtonId();
        int backIndex = kits.isEmpty() ? 1 : kits.size();
        if (selected < 0 || selected >= backIndex) {
            pendingKitEditTarget.remove(player.getUniqueId());
            openAdminKitMenu(player);
            return;
        }
        if (kits.isEmpty()) {
            openAdminKitMenu(player);
            return;
        }

        KitDefinition kit = kits.get(selected);
        pendingKitEditTarget.put(player.getUniqueId(), kit.getKitId());
        openAdminKitItemMenu(player);
    }

    private void openAdminKitItemMenu(Player player) {
        pendingKitRemoveSlots.remove(player.getUniqueId());
        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        Optional<KitDefinition> kitOptional = kitRegistry.findKit(kitId);
        if (kitOptional.isEmpty()) {
            pendingKitEditTarget.remove(player.getUniqueId());
            sendWarning(player, t(player, "Selected kit no longer exists.", "Ausgewaehltes Kit existiert nicht mehr."));
            openAdminKitItemKitSelect(player);
            return;
        }

        KitDefinition kit = kitOptional.get();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Kit Item Manager", "Kit-Item Manager"),
                TextFormat.GRAY + t(player, "Kit", "Kit") + ": " + TextFormat.YELLOW + kit.getDisplayName()
                        + "\n"
                        + TextFormat.GRAY + t(player, "Stored items", "Gespeicherte Items") + ": "
                        + TextFormat.WHITE + kit.getInventoryItems().size()
        );
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Apply Current Inventory + Armor",
                "Overwrite this kit with your current loadout",
                "Aktuelles Inventar + Ruestung uebernehmen",
                "Dieses Kit mit deinem aktuellen Loadout ueberschreiben")));
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Add / Replace Item",
                "Use held item and assign a target slot",
                "Item hinzufuegen / ersetzen",
                "Hand-Item auf Zielslot speichern")));
        form.addButton(new ElementButton(button(player, TextFormat.RED,
                "Remove Item",
                "Remove one item directly from ordered list",
                "Item entfernen",
                "Ein Item direkt aus sortierter Liste entfernen")));
        form.addButton(new ElementButton(button(player, TextFormat.YELLOW,
                "Item Overview",
                "Inspect all slots of this kit",
                "Item-Uebersicht",
                "Alle Slots dieses Kits anzeigen")));
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Armor Management",
                "Set or remove helmet/chest/legs/boots",
                "Ruestungsverwaltung",
                "Helm/Brust/Hose/Schuhe setzen oder entfernen")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_ITEM_MENU);
    }

    private void handleAdminKitItemMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        switch (simple.getClickedButtonId()) {
            case 0 -> applyCurrentLoadoutToEditingKit(player);
            case 1 -> openAdminKitItemAdd(player);
            case 2 -> openAdminKitItemRemove(player);
            case 3 -> openAdminKitItemOverview(player);
            case 4 -> openAdminKitArmorMenu(player);
            default -> {
                pendingKitEditTarget.remove(player.getUniqueId());
                openAdminKitMenu(player);
            }
        }
    }

    private void applyCurrentLoadoutToEditingKit(Player player) {
        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        Optional<KitDefinition> kitOptional = kitRegistry.findKit(kitId);
        if (kitOptional.isEmpty()) {
            pendingKitEditTarget.remove(player.getUniqueId());
            sendWarning(player, t(player, "Selected kit no longer exists.", "Ausgewaehltes Kit existiert nicht mehr."));
            openAdminKitItemKitSelect(player);
            return;
        }

        KitDefinition kit = kitOptional.get();
        boolean updated = kitRegistry.createOrUpdateKitFromInventory(kit.getKitId(), kit.getDisplayName(), player);
        if (updated) {
            sendSuccess(player, t(player,
                    "Kit updated from current inventory and armor.",
                    "Kit mit aktuellem Inventar und Ruestung aktualisiert."));
        } else {
            sendError(player, t(player,
                    "Kit update failed.",
                    "Kit-Aktualisierung fehlgeschlagen."));
        }
        openAdminKitItemMenu(player);
    }

    private void openAdminKitItemAdd(Player player) {
        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        FormWindowCustom form = new FormWindowCustom(title(player, "Add / Replace Kit Item", "Kit-Item hinzufuegen / ersetzen"));
        form.addElement(new ElementLabel(TextFormat.GRAY + t(player,
                "Hold the item in your hand, set slot 0-35, then apply.",
                "Halte das Item in der Hand, setze Slot 0-35 und bestaetige.")
                + "\n" + TextFormat.DARK_GRAY + t(player, "Current hand", "Aktuelle Hand") + ": "
                + TextFormat.WHITE + describeHandItem(player)));
        form.addElement(new ElementInput(t(player, "Target Slot (0-35)", "Zielslot (0-35)"), "0"));
        form.addElement(new ElementDropdown(
                t(player, "Action", "Aktion"),
                List.of(t(player, "Apply Item", "Item anwenden"), t(player, "Back", "Zurueck")),
                0
        ));
        player.showFormWindow(form, FORM_ADMIN_KIT_ITEM_ADD);
    }

    private void handleAdminKitItemAdd(Player player, FormResponse response) {
        if (!(response instanceof FormResponseCustom custom)) {
            return;
        }
        int action = getDropdownIndex(custom, 2);
        if (action == 1) {
            openAdminKitItemMenu(player);
            return;
        }

        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null || kitRegistry.findKit(kitId).isEmpty()) {
            pendingKitEditTarget.remove(player.getUniqueId());
            sendWarning(player, t(player, "Selected kit no longer exists.", "Ausgewaehltes Kit existiert nicht mehr."));
            openAdminKitItemKitSelect(player);
            return;
        }

        Integer slot = parseHotbarSlot(custom.getInputResponse(1));
        if (slot == null) {
            sendError(player, t(player, "Slot must be between 0 and 35.", "Slot muss zwischen 0 und 35 liegen."));
            openAdminKitItemAdd(player);
            return;
        }

        var handItem = player.getInventory().getItemInHand();
        if (handItem == null || handItem.isNull()) {
            sendWarning(player, t(player, "Hold an item in your hand first.", "Halte zuerst ein Item in der Hand."));
            openAdminKitItemAdd(player);
            return;
        }

        boolean updated = kitRegistry.upsertInventoryItem(kitId, slot, handItem);
        if (updated) {
            sendSuccess(player, t(player, "Item saved in slot ", "Item auf Slot ") + slot + ".");
            openAdminKitItemMenu(player);
        } else {
            sendError(player, t(player, "Item could not be saved.", "Item konnte nicht gespeichert werden."));
            openAdminKitItemAdd(player);
        }
    }

    private void openAdminKitItemRemove(Player player) {
        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        Optional<KitDefinition> kitOptional = kitRegistry.findKit(kitId);
        if (kitOptional.isEmpty()) {
            pendingKitEditTarget.remove(player.getUniqueId());
            sendWarning(player, t(player, "Selected kit no longer exists.", "Ausgewaehltes Kit existiert nicht mehr."));
            openAdminKitItemKitSelect(player);
            return;
        }

        KitDefinition kit = kitOptional.get();
        List<KitItem> orderedItems = kit.getInventoryItems().stream()
                .filter(item -> item.getSlot() != null)
                .sorted(Comparator.comparing(KitItem::getSlot))
                .toList();

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Remove Kit Item", "Kit-Item entfernen"),
                section(player,
                        "Select one stored item to remove.",
                        "Waehle ein gespeichertes Item zum Entfernen.")
        );

        List<Integer> slots = new ArrayList<>();
        if (orderedItems.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No stored inventory items", "Keine gespeicherten Inventar-Items")));
        } else {
            for (KitItem item : orderedItems) {
                int slot = item.getSlot();
                slots.add(slot);
                form.addButton(new ElementButton(
                        TextFormat.YELLOW + "Slot " + slot + "\n"
                                + TextFormat.GRAY + formatKitItemLabel(player, item)
                ));
            }
        }
        pendingKitRemoveSlots.put(player.getUniqueId(), slots);
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_ITEM_REMOVE);
    }

    private void handleAdminKitItemRemove(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }

        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null || kitRegistry.findKit(kitId).isEmpty()) {
            pendingKitEditTarget.remove(player.getUniqueId());
            pendingKitRemoveSlots.remove(player.getUniqueId());
            sendWarning(player, t(player, "Selected kit no longer exists.", "Ausgewaehltes Kit existiert nicht mehr."));
            openAdminKitItemKitSelect(player);
            return;
        }

        List<Integer> slots = pendingKitRemoveSlots.getOrDefault(player.getUniqueId(), List.of());
        int selected = simple.getClickedButtonId();
        int backIndex = slots.isEmpty() ? 1 : slots.size();
        if (selected < 0 || selected >= backIndex) {
            pendingKitRemoveSlots.remove(player.getUniqueId());
            openAdminKitItemMenu(player);
            return;
        }

        if (slots.isEmpty()) {
            openAdminKitItemMenu(player);
            return;
        }

        int slot = slots.get(selected);
        boolean removed = kitRegistry.removeInventoryItem(kitId, slot);
        if (removed) {
            sendSuccess(player, t(player, "Item removed from slot ", "Item von Slot entfernt ") + slot + ".");
            openAdminKitItemRemove(player);
        } else {
            sendWarning(player, t(player, "No item stored in this slot.", "In diesem Slot ist kein Item gespeichert."));
            openAdminKitItemRemove(player);
        }
    }

    private void openAdminKitItemOverview(Player player) {
        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        Optional<KitDefinition> kitOptional = kitRegistry.findKit(kitId);
        if (kitOptional.isEmpty()) {
            pendingKitEditTarget.remove(player.getUniqueId());
            sendWarning(player, t(player, "Selected kit no longer exists.", "Ausgewaehltes Kit existiert nicht mehr."));
            openAdminKitItemKitSelect(player);
            return;
        }

        KitDefinition kit = kitOptional.get();
        StringBuilder content = new StringBuilder();
        if (kit.getInventoryItems().isEmpty()) {
            content.append(TextFormat.GRAY).append(t(player, "No inventory items stored.", "Keine Inventar-Items gespeichert."));
        } else {
            kit.getInventoryItems().stream()
                    .sorted(Comparator.comparing(item -> item.getSlot() == null ? Integer.MAX_VALUE : item.getSlot()))
                    .forEach(item -> content.append(TextFormat.YELLOW)
                            .append("Slot ").append(item.getSlot() == null ? "-" : item.getSlot())
                            .append(TextFormat.GRAY)
                            .append(" -> ").append(formatKitItemLabel(player, item))
                            .append("\n"));
        }

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Kit Item Overview", "Kit-Item Uebersicht"),
                content.toString().trim()
        );
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_ITEM_OVERVIEW);
    }

    private void openAdminKitArmorMenu(Player player) {
        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        Optional<KitDefinition> kitOptional = kitRegistry.findKit(kitId);
        if (kitOptional.isEmpty()) {
            pendingKitEditTarget.remove(player.getUniqueId());
            sendWarning(player, t(player, "Selected kit no longer exists.", "Ausgewaehltes Kit existiert nicht mehr."));
            openAdminKitItemKitSelect(player);
            return;
        }

        FormWindowSimple form = new FormWindowSimple(
                title(player, "Armor Management", "Ruestungsverwaltung"),
                TextFormat.GRAY + t(player, "Current armor setup:", "Aktuelles Ruestungs-Setup:") + "\n"
                        + TextFormat.WHITE + kitRegistry.describeArmor(kitOptional.get())
        );
        form.addButton(new ElementButton(button(player, TextFormat.GREEN,
                "Apply Equipped Armor",
                "Copy your currently worn armor into kit",
                "Getragene Ruestung uebernehmen",
                "Aktuell getragene Ruestung ins Kit uebernehmen")));
        form.addButton(new ElementButton(button(player, TextFormat.AQUA,
                "Set Single Armor Slot",
                "Use held item for one armor slot",
                "Einzelnen Ruestungsslot setzen",
                "Hand-Item fuer einen Ruestungsslot nutzen")));
        form.addButton(new ElementButton(button(player, TextFormat.RED,
                "Remove Single Armor Slot",
                "Clear one armor slot from kit",
                "Einzelnen Ruestungsslot entfernen",
                "Einen Ruestungsslot aus Kit entfernen")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_ARMOR_MENU);
    }

    private void handleAdminKitArmorMenu(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }

        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }

        switch (simple.getClickedButtonId()) {
            case 0 -> {
                boolean applied = kitRegistry.applyEquippedArmor(kitId, player);
                if (applied) {
                    sendSuccess(player, t(player, "Equipped armor copied into kit.", "Getragene Ruestung ins Kit kopiert."));
                } else {
                    sendError(player, t(player, "Armor snapshot failed.", "Ruestungs-Snapshot fehlgeschlagen."));
                }
                openAdminKitArmorMenu(player);
            }
            case 1 -> openAdminKitArmorSet(player);
            case 2 -> openAdminKitArmorRemove(player);
            default -> openAdminKitItemMenu(player);
        }
    }

    private void openAdminKitArmorSet(Player player) {
        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        FormWindowCustom form = new FormWindowCustom(title(player, "Set Armor Slot", "Ruestungsslot setzen"));
        form.addElement(new ElementLabel(TextFormat.GRAY + t(player,
                "Hold an armor item in your hand and choose target slot.",
                "Halte ein Ruestungsitem in der Hand und waehle den Zielslot.")
                + "\n" + TextFormat.DARK_GRAY + t(player, "Current hand", "Aktuelle Hand") + ": "
                + TextFormat.WHITE + describeHandItem(player)));
        form.addElement(new ElementDropdown(
                t(player, "Armor Slot", "Ruestungsslot"),
                List.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"),
                0
        ));
        form.addElement(new ElementDropdown(
                t(player, "Action", "Aktion"),
                List.of(t(player, "Apply", "Anwenden"), t(player, "Back", "Zurueck")),
                0
        ));
        player.showFormWindow(form, FORM_ADMIN_KIT_ARMOR_SET);
    }

    private void handleAdminKitArmorSet(Player player, FormResponse response) {
        if (!(response instanceof FormResponseCustom custom)) {
            return;
        }
        int action = getDropdownIndex(custom, 2);
        if (action == 1) {
            openAdminKitArmorMenu(player);
            return;
        }

        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }

        KitRegistry.ArmorSlot slot = resolveArmorSlot(getDropdownIndex(custom, 1));
        if (slot == null) {
            sendError(player, t(player, "Invalid armor slot.", "Ungueltiger Ruestungsslot."));
            openAdminKitArmorSet(player);
            return;
        }

        var hand = player.getInventory().getItemInHand();
        if (hand == null || hand.isNull()) {
            sendWarning(player, t(player, "Hold an item in your hand first.", "Halte zuerst ein Item in der Hand."));
            openAdminKitArmorSet(player);
            return;
        }

        boolean updated = kitRegistry.setArmorItemFromHand(kitId, slot, hand);
        if (updated) {
            sendSuccess(player, t(player, "Armor slot updated: ", "Ruestungsslot aktualisiert: ") + slot.name());
            openAdminKitArmorMenu(player);
        } else {
            sendError(player, t(player, "Armor slot update failed.", "Ruestungsslot-Aktualisierung fehlgeschlagen."));
            openAdminKitArmorSet(player);
        }
    }

    private void openAdminKitArmorRemove(Player player) {
        FormWindowCustom form = new FormWindowCustom(title(player, "Remove Armor Slot", "Ruestungsslot entfernen"));
        form.addElement(new ElementDropdown(
                t(player, "Armor Slot", "Ruestungsslot"),
                List.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"),
                0
        ));
        form.addElement(new ElementDropdown(
                t(player, "Action", "Aktion"),
                List.of(t(player, "Remove", "Entfernen"), t(player, "Back", "Zurueck")),
                0
        ));
        player.showFormWindow(form, FORM_ADMIN_KIT_ARMOR_REMOVE);
    }

    private void handleAdminKitArmorRemove(Player player, FormResponse response) {
        if (!(response instanceof FormResponseCustom custom)) {
            return;
        }
        int action = getDropdownIndex(custom, 1);
        if (action == 1) {
            openAdminKitArmorMenu(player);
            return;
        }

        String kitId = pendingKitEditTarget.get(player.getUniqueId());
        if (kitId == null) {
            openAdminKitItemKitSelect(player);
            return;
        }
        KitRegistry.ArmorSlot slot = resolveArmorSlot(getDropdownIndex(custom, 0));
        if (slot == null) {
            sendError(player, t(player, "Invalid armor slot.", "Ungueltiger Ruestungsslot."));
            openAdminKitArmorRemove(player);
            return;
        }

        boolean removed = kitRegistry.removeArmorItem(kitId, slot);
        if (removed) {
            sendSuccess(player, t(player, "Armor slot cleared: ", "Ruestungsslot geleert: ") + slot.name());
            openAdminKitArmorMenu(player);
        } else {
            sendWarning(player, t(player, "No armor item stored in this slot.", "In diesem Ruestungsslot ist kein Item gespeichert."));
            openAdminKitArmorRemove(player);
        }
    }

    private void openAdminKitDelete(Player player) {
        List<KitDefinition> kits = sortedKits();
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Delete Kit", "Kit loeschen"),
                section(player, "Select a kit to delete.", "Waehle ein Kit zum Loeschen.")
        );
        if (kits.isEmpty()) {
            form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "No kits available", "Keine Kits verfuegbar")));
        } else {
            for (KitDefinition kit : kits) {
                form.addButton(new ElementButton(TextFormat.YELLOW + kit.getDisplayName()));
            }
        }
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_DELETE);
    }

    private void handleAdminKitDelete(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        List<KitDefinition> kits = sortedKits();
        int selected = simple.getClickedButtonId();
        int backIndex = kits.isEmpty() ? 1 : kits.size();
        if (selected < 0 || selected >= backIndex) {
            openAdminKitMenu(player);
            return;
        }
        if (kits.isEmpty()) {
            openAdminKitMenu(player);
            return;
        }
        KitDefinition selectedKit = kits.get(selected);
        pendingKitDeleteTarget.put(player.getUniqueId(), selectedKit.getKitId());
        openAdminKitDeleteConfirm(player, selectedKit.getDisplayName());
    }

    private void openAdminKitDeleteConfirm(Player player, String kitName) {
        FormWindowSimple form = new FormWindowSimple(
                title(player, "Confirm Kit Deletion", "Kit-Loeschung bestaetigen"),
                TextFormat.RED + t(player, "This will permanently delete kit ", "Diese Aktion loescht das Kit dauerhaft ")
                        + TextFormat.YELLOW + kitName + TextFormat.RED + "."
        );
        form.addButton(new ElementButton(TextFormat.RED + t(player, "Delete Now", "Jetzt loeschen")));
        form.addButton(new ElementButton(TextFormat.YELLOW + t(player, "Undo Last Delete", "Letzte Loeschung rueckgaengig")));
        form.addButton(new ElementButton(TextFormat.DARK_GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_DELETE_CONFIRM);
    }

    private void handleAdminKitDeleteConfirm(Player player, FormResponse response) {
        if (!(response instanceof FormResponseSimple simple)) {
            return;
        }
        if (simple.getClickedButtonId() == 1) {
            if (lastDeletedKit != null && kitRegistry.restoreKit(lastDeletedKit)) {
                sendSuccess(player, t(player, "Last deleted kit restored.", "Letztes geloeschtes Kit wiederhergestellt."));
            } else {
                sendWarning(player, t(player, "No deletable kit in undo buffer.", "Kein Kit-Eintrag im Undo-Puffer."));
            }
            openAdminKitDelete(player);
            return;
        }
        if (simple.getClickedButtonId() != 0) {
            pendingKitDeleteTarget.remove(player.getUniqueId());
            openAdminKitDelete(player);
            return;
        }

        String kitId = pendingKitDeleteTarget.remove(player.getUniqueId());
        if (kitId == null) {
            openAdminKitDelete(player);
            return;
        }

        Optional<KitDefinition> removed = kitRegistry.removeKit(kitId);
        if (removed.isPresent()) {
            lastDeletedKit = removed.get();
            sendSuccess(player, t(player, "Kit deleted. Undo is available.", "Kit geloescht. Undo ist verfuegbar."));
        } else {
            sendError(player, t(player, "Kit could not be deleted.", "Kit konnte nicht geloescht werden."));
        }
        openAdminKitDelete(player);
    }

    private void openAdminKitList(Player player) {
        List<KitDefinition> kits = sortedKits();
        StringBuilder content = new StringBuilder();
        if (kits.isEmpty()) {
            content.append(TextFormat.GRAY).append(t(player, "No kits available.", "Keine Kits verfuegbar."));
        } else {
            for (KitDefinition kit : kits) {
                content.append(TextFormat.YELLOW).append(kit.getDisplayName()).append("\n");
            }
        }
        FormWindowSimple form = new FormWindowSimple(title(player, "Kit List", "Kit Liste"), content.toString().trim());
        form.addButton(new ElementButton(TextFormat.GRAY + t(player, "Back", "Zurueck")));
        player.showFormWindow(form, FORM_ADMIN_KIT_LIST);
    }

    private String title(Player player, String english, String german) {
        return TextFormat.GOLD + "FFA" + TextFormat.GRAY + " | " + TextFormat.WHITE + t(player, english, german);
    }

    private String section(Player player, String english, String german) {
        return TextFormat.GRAY + t(player, english, german);
    }

    private String button(Player player, TextFormat color, String enTitle, String enSubtitle, String deTitle, String deSubtitle) {
        return color + t(player, enTitle, deTitle) + "\n" + TextFormat.GRAY + t(player, enSubtitle, deSubtitle);
    }

    private String formatStatus(Player player, ArenaStatus status) {
        return switch (status) {
            case WAITING -> t(player, "Standby", "Standby");
            case ACTIVE -> t(player, "Live", "Live");
            case DISABLED -> t(player, "Offline", "Offline");
            case MAINTENANCE -> t(player, "Maintenance", "Wartung");
        };
    }

    private String formatStatusBadge(Player player, ArenaStatus status) {
        return switch (status) {
            case ACTIVE -> TextFormat.GREEN + "[LIVE]";
            case WAITING -> TextFormat.YELLOW + "[STANDBY]";
            case MAINTENANCE -> TextFormat.GOLD + "[MAINT]";
            case DISABLED -> TextFormat.RED + "[OFFLINE]";
        } + " " + TextFormat.WHITE + formatStatus(player, status);
    }

    private String formatJoinBadge(Player player, ArenaStatus status) {
        if (status == ArenaStatus.ACTIVE) {
            return TextFormat.GREEN + t(player, "Open", "Offen");
        }
        return TextFormat.RED + t(player, "Closed", "Gesperrt");
    }

    private String formatReadinessBadge(Player player, ArenaDefinition arena) {
        Optional<String> readinessError = arenaCatalogService.validateActivationReadiness(arena);
        if (readinessError.isEmpty()) {
            return TextFormat.GREEN + t(player, "Ready", "Bereit");
        }
        return TextFormat.RED + t(player, "Not Ready", "Nicht bereit")
                + TextFormat.GRAY + " (" + localizeReadinessReason(player, readinessError.get()) + ")";
    }

    private String localizeReadinessReason(Player player, String reason) {
        if (reason == null || reason.isBlank()) {
            return t(player, "Unknown readiness error.", "Unbekannter Bereitschaftsfehler.");
        }
        return switch (reason) {
            case "Arena not found." -> t(player, "Arena not found.", "Arena nicht gefunden.");
            case "No spawn points configured." -> t(player, "No spawn points configured.", "Keine Spawnpunkte konfiguriert.");
            case "World name is missing." -> t(player, "World name is missing.", "Weltname fehlt.");
            case "Safe-zone reference is invalid." -> t(player, "Safe-zone reference is invalid.", "Safe-Zone Referenz ist ungueltig.");
            default -> {
                if (reason.startsWith("World '") && reason.endsWith("' is not loadable.")) {
                    String worldName = reason.substring("World '".length(), reason.length() - "' is not loadable.".length());
                    yield t(player,
                            "World '" + worldName + "' is not loadable.",
                            "Welt '" + worldName + "' kann nicht geladen werden.");
                }
                yield reason;
            }
        };
    }

    private String t(Player player, String english, String german) {
        PlayerLanguage language = playerProfileService.getLanguage(player.getUniqueId());
        return localizationService.resolve(language, english, german);
    }

    private void sendSuccess(Player player, String message) {
        player.sendMessage(FfaMessageFormatter.success(message));
    }

    private void sendInfo(Player player, String message) {
        player.sendMessage(FfaMessageFormatter.info(message));
    }

    private void sendWarning(Player player, String message) {
        player.sendMessage(FfaMessageFormatter.warning(message));
    }

    private void sendError(Player player, String message) {
        player.sendMessage(FfaMessageFormatter.error(message));
    }

    private int getDropdownIndex(FormResponseCustom custom, int elementIndex) {
        FormResponseData data = custom.getDropdownResponse(elementIndex);
        return data == null ? -1 : data.getElementID();
    }

    private Integer parseHotbarSlot(String rawInput) {
        String normalized = trimInput(rawInput);
        if (normalized == null) {
            return null;
        }
        try {
            int slot = Integer.parseInt(normalized);
            if (slot < 0 || slot > 35) {
                return null;
            }
            return slot;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private KitRegistry.ArmorSlot resolveArmorSlot(int dropdownIndex) {
        return switch (dropdownIndex) {
            case 0 -> KitRegistry.ArmorSlot.HELMET;
            case 1 -> KitRegistry.ArmorSlot.CHESTPLATE;
            case 2 -> KitRegistry.ArmorSlot.LEGGINGS;
            case 3 -> KitRegistry.ArmorSlot.BOOTS;
            default -> null;
        };
    }

    private String describeHandItem(Player player) {
        var hand = player.getInventory().getItemInHand();
        if (hand == null || hand.isNull()) {
            return t(player, "empty", "leer");
        }
        return hand.getName() + " x" + hand.getCount();
    }

    private String resolveArenaName(Player player, String arenaId) {
        if (arenaId == null || arenaId.isBlank()) {
            return t(player, "Unknown Arena", "Unbekannte Arena");
        }
        return arenaCatalogService.findArena(arenaId)
                .map(ArenaDefinition::getDisplayName)
                .orElse(t(player, "Unknown Arena", "Unbekannte Arena"));
    }

    private String resolveKitName(Player player, String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return null;
        }
        return kitRegistry.findKit(kitId)
                .map(KitDefinition::getDisplayName)
                .orElse(t(player, "Unknown Kit", "Unbekanntes Kit"));
    }

    private String formatKitItemLabel(Player player, KitItem item) {
        Item preview = Item.get(item.getItemId(), item.getDamage(), Math.max(1, item.getAmount()));
        String itemName = (preview == null || preview.isNull()) ? t(player, "Unknown Item", "Unbekanntes Item") : preview.getName();
        return itemName + " x" + item.getAmount();
    }

    private String normalizeIdentifier(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private String trimInput(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<ArenaDefinition> sortedArenas() {
        return arenaCatalogService.listArenas().stream()
                .sorted(Comparator.comparing(ArenaDefinition::getArenaId, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<KitDefinition> sortedKits() {
        return kitRegistry.listKits().stream()
                .sorted(Comparator.comparing(KitDefinition::getKitId, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private enum PlayerMainAction {
        ARENA,
        KITS,
        STATS,
        SETTINGS,
        CLOSE
    }

    private enum PlayerStatsAction {
        PERSONAL,
        GLOBAL,
        ARENA,
        BACK
    }
}



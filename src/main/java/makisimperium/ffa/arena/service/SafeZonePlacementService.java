package makisimperium.ffa.arena.service;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.math.Vector3;
import makisimperium.ffa.arena.model.ArenaDefinition;
import makisimperium.ffa.arena.model.CuboidZone;
import makisimperium.ffa.messaging.FfaMessageFormatter;
import makisimperium.ffa.player.model.PlayerLanguage;
import makisimperium.ffa.player.service.PlayerProfileService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SafeZonePlacementService {

    public static final String SAFE_ZONE_IDENTIFIER = "safe_zone";

    private final ArenaCatalogService arenaCatalogService;
    private final PlayerProfileService playerProfileService;
    private final Map<UUID, PlacementSession> placementSessions = new ConcurrentHashMap<>();

    public SafeZonePlacementService(
            ArenaCatalogService arenaCatalogService,
            PlayerProfileService playerProfileService
    ) {
        this.arenaCatalogService = arenaCatalogService;
        this.playerProfileService = playerProfileService;
    }

    public boolean beginPlacement(Player player, String arenaId) {
        Optional<ArenaDefinition> arenaOptional = arenaCatalogService.findArena(arenaId);
        if (arenaOptional.isEmpty()) {
            return false;
        }

        PlacementSession session = new PlacementSession();
        session.arenaId = arenaOptional.get().getArenaId();
        placementSessions.put(player.getUniqueId(), session);

        player.sendMessage(FfaMessageFormatter.info(t(player,
                "Safe-Zone mode enabled. Click block #1.",
                "Safe-Zone Modus aktiviert. Klicke Block #1.")));
        return true;
    }

    public boolean isPlacing(UUID uniqueId) {
        return placementSessions.containsKey(uniqueId);
    }

    public void cancel(UUID uniqueId) {
        placementSessions.remove(uniqueId);
    }

    public PlacementResult handleBlockInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlacementSession session = placementSessions.get(player.getUniqueId());
        if (session == null) {
            return PlacementResult.IGNORED;
        }

        PlayerInteractEvent.Action action = event.getAction();
        if (action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
                && action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return PlacementResult.IGNORED;
        }
        if (event.getBlock() == null || event.getBlock().getLevel() == null) {
            return PlacementResult.IGNORED;
        }

        event.setCancelled(true);

        String clickedWorld = event.getBlock().getLevel().getName();
        Vector3 clicked = new Vector3(event.getBlock().getFloorX(), event.getBlock().getFloorY(), event.getBlock().getFloorZ());

        if (session.firstPosition == null) {
            session.worldName = clickedWorld;
            session.firstPosition = clicked;
            player.sendMessage(FfaMessageFormatter.info(t(player,
                    "Safe-Zone point #1 confirmed. Click block #2.",
                    "Safe-Zone Punkt #1 bestaetigt. Klicke Block #2.")));
            return PlacementResult.FIRST_POINT_SET;
        }

        if (!session.worldName.equalsIgnoreCase(clickedWorld)) {
            placementSessions.remove(player.getUniqueId());
            player.sendMessage(FfaMessageFormatter.error(t(player,
                    "Safe-Zone cancelled: both points must be in the same world.",
                    "Safe-Zone abgebrochen: Beide Punkte muessen in derselben Welt sein.")));
            return PlacementResult.COMPLETED_FAILED;
        }

        Optional<ArenaDefinition> arenaOptional = arenaCatalogService.findArena(session.arenaId);
        if (arenaOptional.isEmpty()) {
            placementSessions.remove(player.getUniqueId());
            player.sendMessage(FfaMessageFormatter.error(t(player,
                    "Safe-Zone cancelled: arena no longer exists.",
                    "Safe-Zone abgebrochen: Arena existiert nicht mehr.")));
            return PlacementResult.COMPLETED_FAILED;
        }

        ArenaDefinition arena = arenaOptional.get();
        if (!arena.getWorldName().equalsIgnoreCase(session.worldName)) {
            placementSessions.remove(player.getUniqueId());
            player.sendMessage(FfaMessageFormatter.error(t(player,
                    "Safe-Zone cancelled: selected world does not match arena world.",
                    "Safe-Zone abgebrochen: Gewaehlte Welt passt nicht zur Arena-Welt.")));
            return PlacementResult.COMPLETED_FAILED;
        }

        CuboidZone safeZone = CuboidZone.fromCorners(session.worldName, session.firstPosition, clicked);
        boolean saved = arenaCatalogService.upsertZone(arena.getArenaId(), SAFE_ZONE_IDENTIFIER, safeZone)
                && arenaCatalogService.setSafeZone(arena.getArenaId(), SAFE_ZONE_IDENTIFIER);

        placementSessions.remove(player.getUniqueId());

        if (saved) {
            player.sendMessage(FfaMessageFormatter.success(t(player,
                    "Safe-Zone saved for arena " + arena.getDisplayName() + ".",
                    "Safe-Zone fuer Arena " + arena.getDisplayName() + " gespeichert.")));
            return PlacementResult.COMPLETED_SUCCESS;
        } else {
            player.sendMessage(FfaMessageFormatter.error(t(player,
                    "Safe-Zone could not be saved.",
                    "Safe-Zone konnte nicht gespeichert werden.")));
            return PlacementResult.COMPLETED_FAILED;
        }
    }

    private String t(Player player, String english, String german) {
        PlayerLanguage language = playerProfileService.getLanguage(player.getUniqueId());
        return language == PlayerLanguage.GERMAN ? german : english;
    }

    private static final class PlacementSession {
        private String arenaId;
        private String worldName;
        private Vector3 firstPosition;
    }

    public enum PlacementResult {
        IGNORED,
        FIRST_POINT_SET,
        COMPLETED_SUCCESS,
        COMPLETED_FAILED
    }
}



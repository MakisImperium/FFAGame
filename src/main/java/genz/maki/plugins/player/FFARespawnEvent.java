package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerRespawnEvent;
import genz.maki.plugins.battle.BattleBasis;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.kits.manager.KitsManager;

import java.io.IOException;

public class FFARespawnEvent implements Listener {

    private final RoomConfig config;
    private final BattleBasis battleBasis;
    private final KitsManager kitsManager;

    public FFARespawnEvent(RoomConfig config, BattleBasis battleBasis, KitsManager kitsManager) {
        this.config = config;
        this.battleBasis = battleBasis;
        this.kitsManager = kitsManager;
    }

    /**
     * Handles the event triggered when a player respawns in the game. This method checks
     * if the player is part of a specific list configured in the game settings. If the player
     * is in the list, it retrieves the kit assigned to the player and applies it upon respawning.
     * It ensures the player is equipped with the appropriate items and armor from the kit.
     *
     * @param event The PlayerRespawnEvent that contains information about the player who
     *              has respawned, including a reference to the player object.
     * @throws IOException If an I/O error occurs while checking the player's list
     *                     membership or retrieving their assigned kit.
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) throws IOException {
        Player player = event.getPlayer();
        if (config.isPlayerInList(player)) {
            String kitName = kitsManager.getPlayerKit(player);
            kitsManager.applyKit(player, kitName);
        }
    }
}

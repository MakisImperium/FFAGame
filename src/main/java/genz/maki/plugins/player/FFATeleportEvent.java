package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class FFATeleportEvent implements Listener {

    private final RoomConfig config;

    public FFATeleportEvent(RoomConfig config) {
        this.config = config;
    }

    /**
     * Handles the event triggered when a player teleports. This method checks if the player
     * is in a specific list managed by the config. If the player is found in the list,
     * their inventory is cleared and they are removed from the room configuration.
     *
     * @param event The PlayerTeleportEvent that contains information about the player
     *              who initiated the teleport, including a reference to the player object
     *              for whom the actions are performed.
     * @throws IOException If an I/O error occurs while checking the player's presence in the list
     *                     or during the removal process from the room configuration.
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) throws IOException {

        Player player = event.getPlayer();

        if (config.isPlayerInList(player)) {
            player.getInventory().clearAll();

            config.removePlayerFromRoom(player);
        }
    }
}

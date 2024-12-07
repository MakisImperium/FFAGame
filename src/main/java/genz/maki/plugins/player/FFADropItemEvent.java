package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDropItemEvent;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class FFADropItemEvent implements Listener {

    private final RoomConfig config;

    public FFADropItemEvent(RoomConfig config) {
        this.config = config;
    }

    /**
     * Handles the event triggered when a player attempts to drop an item. This event checks
     * if the player is part of a specific list maintained in the configuration. If so, the
     * item drop action is canceled.
     *
     * @param event The PlayerDropItemEvent containing details of the item drop attempt, including
     *              a reference to the player who is attempting to drop the item.
     * @throws IOException If an I/O error occurs while checking the player's membership in the list.
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) throws IOException {
        Player player = event.getPlayer();

        if (config.isPlayerInList(player)) {
            event.setCancelled(true);
        }
    }
}

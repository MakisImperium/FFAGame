package genz.maki.plugins.player;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class FFABlockBreak implements Listener {

    private final RoomConfig config;

    public FFABlockBreak(RoomConfig config) {
        this.config = config;
    }

    /**
     * Handles the event that occurs when a block is broken in the game. This method verifies
     * if the player who broke the block is part of a preset list of players. If the player
     * is in the list, the block break event is canceled, preventing the block from being broken.
     *
     * @param event The BlockBreakEvent that contains information about the block breaking
     *              action, including the player who initiated it.
     * @throws IOException If an I/O error occurs while checking if the player is in the list.
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event) throws IOException {
        if (config.isPlayerInList(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

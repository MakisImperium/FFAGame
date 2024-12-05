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

    @EventHandler
    public void onBreak(BlockBreakEvent event) throws IOException {
        if (config.isPlayerInList(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}

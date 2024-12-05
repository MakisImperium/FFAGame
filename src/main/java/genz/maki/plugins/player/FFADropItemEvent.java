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

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) throws IOException {
        Player player = event.getPlayer();

        if (config.isPlayerInList(player)) {
            event.setCancelled(true);
        }
    }
}

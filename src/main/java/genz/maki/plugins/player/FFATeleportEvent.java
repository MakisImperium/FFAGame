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

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) throws IOException {

        Player player = event.getPlayer();

        if (config.isPlayerInList(player)) {
            player.getInventory().clearAll();

            config.removePlayerFromRoom(player);
        }
    }
}

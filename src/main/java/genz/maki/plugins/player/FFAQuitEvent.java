package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import genz.maki.plugins.Main;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class FFAQuitEvent implements Listener {

    private final RoomConfig config;
    private final Main plugin;

    public FFAQuitEvent(RoomConfig config, Main plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info(player.getUniqueId().toString() + " has quit the game");
        player.getInventory().clearAll();
        try {
            if (config.isPlayerInList(player)) {
                config.removePlayerFromRoom(player);
                player.getInventory().clearAll();
                plugin.getLogger().info("Player " + player.getUniqueId().toString() + " has been removed from room");
            }
        } catch (IOException e) {
            plugin.getLogger().error("An error occurred while removing player from room: " + e.getMessage());
        }
    }
}

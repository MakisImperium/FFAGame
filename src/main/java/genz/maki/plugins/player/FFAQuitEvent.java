package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import genz.maki.plugins.Main;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.kits.manager.KitsManager;

import java.io.IOException;

public class FFAQuitEvent implements Listener {

    private final RoomConfig config;
    private final Main plugin;
    private final KitsManager kitsManager;

    public FFAQuitEvent(RoomConfig config, Main plugin, KitsManager kitsManager) {
        this.config = config;
        this.plugin = plugin;
        this.kitsManager = kitsManager;
    }

    /**
     * Handles the event triggered when a player quits the game. This method performs
     * cleanup tasks by clearing the player's inventory and removing the player from
     * a specific room if they are part of a configured list. It logs the player's
     * unique identifier upon quitting and provides error logging if any I/O issues
     * occur during the removal process.
     *
     * @param event The PlayerQuitEvent containing details about the player quitting
     *              the game, including a reference to the player object that can
     *              be used to perform cleanup operations.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clearAll();
        try {
            if (config.isPlayerInList(player)) {
                config.removePlayerFromRoom(player);

                kitsManager.removePlayerFromKitList(player);
                kitsManager.removePlayerFromList(player);

                player.getInventory().clearAll();
                plugin.getLogger().info("Player " + player.getUniqueId().toString() + " has been removed from room");
            }
        } catch (IOException e) {
            plugin.getLogger().error("An error occurred while removing player from room: " + e.getMessage());
        }
    }
}

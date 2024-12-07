package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class FFAFallDamage implements Listener {

    private final RoomConfig config;

    public FFAFallDamage(RoomConfig config) {
        this.config = config;
    }

    /**
     * Handles fall damage events for entities. This method checks if the entity
     * involved in the event is a player. If the player is part of a specified list
     * (indicated by the configuration), the fall damage event is canceled.
     *
     * @param event The EntityDamageEvent that contains details about the entity
     *              receiving damage, specifically fall damage. This event will
     *              be canceled if the player is confirmed to be in the "players-playing" list.
     * @throws IOException If there is an error while checking the player's membership
     *                     in the list due to I/O issues.
     */
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) throws IOException {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (config.isPlayerInList(player)) {
                event.setCancelled(true);
            }
        }
    }
}

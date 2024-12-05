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

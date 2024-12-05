package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class FFADamageEvent implements Listener {

    private final FFAConfig ffaConfig;
    private final PlayerFile playerFile;
    private final RoomConfig roomConfig;

    public FFADamageEvent(FFAConfig ffaConfig, PlayerFile playerFile, RoomConfig roomConfig) {
        this.ffaConfig = ffaConfig;
        this.playerFile = playerFile;
        this.roomConfig = roomConfig;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) throws IOException {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {

            Player player = (Player) event.getEntity();
            Player damagePlayer = (Player) event.getDamager();

            if (!(damagePlayer == player)) {
                if (player.getLocation().getLevel().getName().equalsIgnoreCase(ffaConfig.getWorld()) && damagePlayer.getLocation().getLevel().getName().equalsIgnoreCase(ffaConfig.getWorld())) {
                    if (roomConfig.isPlayerInList(player) && roomConfig.isPlayerInList(damagePlayer)) {
                        return;
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

}

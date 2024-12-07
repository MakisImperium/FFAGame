package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.kits.manager.KitsManager;

import java.io.IOException;

public class FFADamageEvent implements Listener {

    private final FFAConfig ffaConfig;
    private final PlayerFile playerFile;
    private final RoomConfig roomConfig;
    private KitsManager kitsManager;

    public FFADamageEvent(FFAConfig ffaConfig, PlayerFile playerFile, RoomConfig roomConfig, KitsManager kitsManager) {
        this.ffaConfig = ffaConfig;
        this.playerFile = playerFile;
        this.roomConfig = roomConfig;
        this.kitsManager = kitsManager;
    }

    /**
     * Handles damage events where one player damages another player. This method performs several
     * checks to ensure that the event should not be canceled, such as verifying that both players
     * are in the specified FFA world, and checking lists that determine if players are allowed
     * to engage in combat.
     *
     * @param event The EntityDamageByEntityEvent that contains information about the players involved
     *              in the damage event. This includes the entity being damaged and the damager.
     * @throws IOException If an I/O error occurs while checking player membership in lists.
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) throws IOException {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damagePlayer = (Player) event.getDamager();

            if (!damagePlayer.equals(player)) {
                if (player.getLocation().getLevel().getName().equalsIgnoreCase(ffaConfig.getWorld()) &&
                        damagePlayer.getLocation().getLevel().getName().equalsIgnoreCase(ffaConfig.getWorld())) {
                    if (!roomConfig.isPlayerInList(player) || !roomConfig.isPlayerInList(damagePlayer)) {
                        if (kitsManager.isPlayerInKitList(player) || kitsManager.isPlayerInEditList(player)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

}

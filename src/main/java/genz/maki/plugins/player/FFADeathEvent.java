package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.battle.BattleBasis;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.kits.manager.KitsManager;

import java.io.IOException;
import java.util.Map;

public class FFADeathEvent implements Listener {

    private final RoomConfig config;
    private final PlayerFile playerFile;
    private final KitsManager kitsManager;

    public FFADeathEvent(RoomConfig config, PlayerFile playerFile, KitsManager kitsManager) {
        this.config = config;
        this.playerFile = playerFile;
        this.kitsManager = kitsManager;
    }

    
    /**
     * Handles the event triggered when a player dies in the game. It checks if the deceased
     * player and the killer, if applicable, are part of the configured list of players. If so,
     * it updates their respective kill and death counts, sends messages to both the deceased
     * and the killer, and provides the killer with battle items.
     *
     * @param event The PlayerDeathEvent containing information about the player's death.
     * @throws IOException If an I/O error occurs while accessing player data files.
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) throws IOException {
        Player deathPlayer = event.getEntity().getPlayer();
        event.setKeepInventory(true);
        if(config.isPlayerInList(deathPlayer)) {
            event.setKeepInventory(true);
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
              EntityDamageByEntityEvent damageCause = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
               if (damageCause.getDamager() instanceof Player) {
                   Player player = (Player) damageCause.getDamager();
                   if (config.isPlayerInList(player)) {
                       playerFile.addDeath(deathPlayer);
                       playerFile.addKill(player);

                       player.sendTitle("", TextFormat.RED + "You have killed " + player.getName(), 2, 3, 2);
                       player.sendMessage(TextFormat.GREEN + "You have killed " + deathPlayer.getName());
                       deathPlayer.sendMessage(TextFormat.RED + "You have been killed by " + player.getName());

                       String kitName = kitsManager.getPlayerKit(player);
                       kitsManager.applyKit(player, kitName);
                   }
               }
            }
        }
    }
}

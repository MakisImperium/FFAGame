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

import java.io.IOException;
import java.util.Map;

public class FFADeathEvent implements Listener {

    private final RoomConfig config;
    private final PlayerFile playerFile;
    private final BattleBasis battleBasis;

    public FFADeathEvent(RoomConfig config, PlayerFile playerFile, BattleBasis battleBasis) {
        this.config = config;
        this.playerFile = playerFile;
        this.battleBasis = battleBasis;
    }

    
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
                       player.sendMessage(TextFormat.GREEN + "You have killed " + player.getName());
                       deathPlayer.sendMessage(TextFormat.RED + "You have been killed by " + player.getName());

                       battleBasis.giveItemsToBattle(player);
                   }
               }
            }
        }
    }
}

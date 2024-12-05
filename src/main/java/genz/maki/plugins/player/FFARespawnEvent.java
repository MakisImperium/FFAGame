package genz.maki.plugins.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerRespawnEvent;
import genz.maki.plugins.battle.BattleBasis;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class FFARespawnEvent implements Listener {

    private final RoomConfig config;
    private final BattleBasis battleBasis;

    public FFARespawnEvent(RoomConfig config, BattleBasis battleBasis) {
        this.config = config;
        this.battleBasis = battleBasis;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) throws IOException {
        Player player = event.getPlayer();
        if (config.isPlayerInList(player)) {
            battleBasis.giveItemsToBattle(player);
        }
    }
}

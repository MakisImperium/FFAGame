package genz.maki.plugins.battle;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.utils.WorldUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BattleBasis {

    private final Main main;
    private final FFAConfig ffaConfig;
    private final RoomConfig roomConfig;
    private final WorldUtils worldUtils;
    private final PlayerFile playerFile;

    private final String WORLDNAME;
    

    public BattleBasis(Main main, FFAConfig ffaConfig, RoomConfig roomConfig, WorldUtils worldUtils, PlayerFile playerFile) {
        this.main = main;
        this.ffaConfig = ffaConfig;
        this.roomConfig = roomConfig;
        this.worldUtils = worldUtils;
        this.playerFile = playerFile;
        WORLDNAME = this.ffaConfig.getWorld();
    }

    /**
     * Allows a player to join the battle by teleporting them to a specified level,
     * sending a confirmation message, updating room configurations, creating a player file
     * if it does not exist, and equipping them with necessary battle items.
     *
     * @param player The player who intends to join the battle. This object contains
     *               information needed to teleport the player, manage their settings,
     *               and modify their inventory.
     */
    public void joinToBattle(Player player) throws IOException {
        if(this.worldUtils.isWorldLoaded()) {
            Level level = this.main.getServer().getLevelByName(WORLDNAME);
            player.teleport(level.getSafeSpawn());

            player.sendMessage(main.getPrefix() + TextFormat.GREEN + "You have joined the battle!");

            this.roomConfig.addPlayerToRoom(player);
            this.playerFile.createPlayerFile(player);
        }
    }

    /**
     * Removes the player from the battle, clears their inventory, teleports them to a safe spawn,
     * and sends a message indicating they have left the battle.
     *
     * @param player the player who is leaving the battle. The player's inventory is cleared,
     *               they are removed from the room configuration, and they are teleported to the server's default level safe spawn.
     */
    public void leaveBattle(Player player) throws IOException {
        player.getInventory().clearAll();

        this.roomConfig.removePlayerFromRoom(player);

        player.teleport(this.main.getServer().getDefaultLevel().getSafeSpawn());
        player.sendMessage(main.getPrefix() + TextFormat.RED + "You have left the battle!");
    }
    
}

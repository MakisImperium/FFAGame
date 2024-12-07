package genz.maki.plugins.utils;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import genz.maki.plugins.Main;
import genz.maki.plugins.config.FFAConfig;

public class WorldUtils {

    private final Main plugin;
    private final FFAConfig ffaConfig;
    private final String WORLDNAME;

    public WorldUtils(Main plugin, FFAConfig ffaConfig) {
        this.plugin = plugin;
        this.ffaConfig = ffaConfig;
        this.WORLDNAME = this.ffaConfig.getWorld();
    }

    /**
     * Sets the world spawn point to the player's current location.
     *
     * @param level the game level where the spawn point is set
     * @param player the player whose current location will be used as the spawn point
     */
    public void setWorldSpawn(Level level, Player player) {
        level.getSafeSpawn().setX(player.getX());
        level.getSafeSpawn().setY(player.getY());
        level.getSafeSpawn().setZ(player.getZ());
    }

    /**
     * Checks if the specified world is currently loaded on the server.
     *
     * @return true if the world defined by WORLDNAME is loaded on the server; false otherwise
     */
    public boolean isWorldLoaded() {
        if (plugin.getServer().isLevelLoaded(WORLDNAME)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads a specified world by its name. This method uses the server instance
     * from the plugin to perform the world loading operation.
     *
     * @param name the name of the world to be loaded
     */
    public void loadWorld(String name) {
        plugin.getServer().loadLevel(WORLDNAME);
    }

    /**
     * Retrieves the number of players currently present in the specified world.
     *
     * @return An integer representing the number of players in the world defined by WORLDNAME.
     */
    public int getPlayerCountInWorld() {
        return plugin.getServer().getLevelByName(WORLDNAME).getPlayers().size();
    }
}

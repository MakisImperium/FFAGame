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

    public void setWorldSpawn(Level level, Player player) {
        level.getSafeSpawn().setX(player.getX());
        level.getSafeSpawn().setY(player.getY());
        level.getSafeSpawn().setZ(player.getZ());
    }

    public boolean isWorldLoaded() {
        if (plugin.getServer().isLevelLoaded(WORLDNAME)){
            return true;
        } else {
            return false;
        }
    }

    public void loadWorld(String name) {
        plugin.getServer().loadLevel(WORLDNAME);
    }

    public int getPlayerCountInWorld() {
        return plugin.getServer().getLevelByName(WORLDNAME).getPlayers().size();
    }
}

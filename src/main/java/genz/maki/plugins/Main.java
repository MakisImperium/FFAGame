package genz.maki.plugins;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.battle.BattleBasis;
import genz.maki.plugins.commands.FFACommand;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.form.MainForm;
import genz.maki.plugins.form.events.FormRespondEvent;
import genz.maki.plugins.player.*;
import genz.maki.plugins.utils.WorldUtils;

import java.io.File;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends PluginBase {

    private static Config config;
    private static RoomConfig roomConfig;
    private static PlayerFile playerFile;
    private static BattleBasis battleBasis;
    private static FFAConfig ffaConfig;
    private static WorldUtils worldUtils;
    private static MainForm mainForm;

    private static String prefix;

    public static void main(String[] args) {}

    @Override
    public void onEnable() {
        prefix = TextFormat.WHITE + "[" + TextFormat.GOLD + "FFA" + TextFormat.WHITE + "] ";

        this.getLogger().info(getPrefix() + TextFormat.GREEN + "Plugin enabled");

        this.saveDefaultConfig();
        config = new Config(this.getDataFolder() + "/config.yml", Config.YAML);

        File file1 = new File(this.getDataFolder() + "/players");
        File file2 = new File(this.getDataFolder() + "/room");

        if (!file1.exists()) {
            file1.mkdir();
        }
        if (!file2.exists()) {
            file2.mkdir();
        }

        // Create Chache File
        roomConfig = new RoomConfig(this);
        roomConfig.createChacheFile();

        // Initialisiere PlayerFile
        playerFile = new PlayerFile(this);
        // Initialisiere FFAConfig und WorldUtils
        ffaConfig = new FFAConfig(this);
        worldUtils = new WorldUtils(this, ffaConfig);
        // Initialisiere BattleBasis
        battleBasis = new BattleBasis(this, ffaConfig, roomConfig, worldUtils, playerFile);

        // Initialisiere MainForm
        mainForm = new MainForm(this, ffaConfig, playerFile, roomConfig);
        saveDefaultConfig();

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new FFADeathEvent(roomConfig, playerFile, battleBasis), this);
        pluginManager.registerEvents(new FFADamageEvent(ffaConfig, playerFile, roomConfig), this);
        pluginManager.registerEvents(new FFADropItemEvent(roomConfig), this);
        pluginManager.registerEvents(new FFABlockBreak(roomConfig), this);
        pluginManager.registerEvents(new FFAFallDamage(roomConfig), this);
        pluginManager.registerEvents(new FFARespawnEvent(roomConfig, battleBasis), this);
        pluginManager.registerEvents(new FFATeleportEvent(roomConfig), this);
        pluginManager.registerEvents(new FFAQuitEvent(roomConfig, this), this);

        // Form Register

        pluginManager.registerEvents(new FormRespondEvent(ffaConfig, this, roomConfig, battleBasis, mainForm), this);

        // Register Commands

        this.getServer().getCommandMap().register("ffa", new FFACommand(mainForm));
    }

    public static String getPrefix() {
        return prefix;
    }
}
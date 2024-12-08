package genz.maki.plugins;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.battle.BattleBasis;
import genz.maki.plugins.commands.FFAAdminCommand;
import genz.maki.plugins.commands.FFACommand;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.form.KitForm;
import genz.maki.plugins.form.MainForm;
import genz.maki.plugins.form.events.FormRespondEvent;
import genz.maki.plugins.kits.manager.KitsManager;
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
    private static KitsManager kitsManager;
    private static KitForm kitForm;

    private static String prefix;

    public static void main(String[] args) {}

    /**
     * Called when the plugin is enabled. This method initializes necessary configurations,
     * directories, files, and managers required for the plugin's operation. It also registers
     * various events and commands with the server.
     *
     * The method follows these key steps:
     * 1. Sets up a prefix for log messages.
     * 2. Logs an enabling message to the console.
     * 3. Saves and loads the default configuration file.
     * 4. Ensures essential directories (e.g., players, room) are created if absent.
     * 5. Initializes configuration and utility classes such as RoomConfig, PlayerFile,
     *    FFAConfig, WorldUtils, and BattleBasis.
     * 6. Constructs crucial gameplay and admin interfaces like KitsManager, KitForm, and MainForm.
     * 7. Registers multiple event listeners related to game mechanics (e.g., FFA deaths, damage,
     *    item drops, block breaking, etc.).
     * 8. Registers form-related and administration commands within the server's command map.
     */
    @Override
    public void onEnable() {
        prefix = TextFormat.WHITE + "[" + TextFormat.GOLD + "FFA" + TextFormat.WHITE + "] ";

        this.getLogger().info(TextFormat.GREEN + "Plugin enabled successfully! Version " + this.getDescription().getVersion() + "v");

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

        playerFile = new PlayerFile(this);

        ffaConfig = new FFAConfig(this);
        worldUtils = new WorldUtils(this, ffaConfig);
        battleBasis = new BattleBasis(this, ffaConfig, roomConfig, worldUtils, playerFile);

        kitsManager = new KitsManager(this, battleBasis);

        kitForm = new KitForm(this, ffaConfig, playerFile, roomConfig, kitsManager);

        mainForm = new MainForm(this, ffaConfig, playerFile, roomConfig, kitsManager);

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new FFADeathEvent(roomConfig, playerFile, kitsManager), this);
        pluginManager.registerEvents(new FFADamageEvent(ffaConfig, playerFile, roomConfig, kitsManager), this);
        pluginManager.registerEvents(new FFADropItemEvent(roomConfig), this);
        pluginManager.registerEvents(new FFABlockBreak(roomConfig), this);
        pluginManager.registerEvents(new FFAFallDamage(roomConfig), this);
        pluginManager.registerEvents(new FFARespawnEvent(roomConfig, battleBasis, kitsManager), this);
        pluginManager.registerEvents(new FFATeleportEvent(roomConfig), this);
        pluginManager.registerEvents(new FFAQuitEvent(roomConfig, this, kitsManager), this);

        // Form Register

        pluginManager.registerEvents(new FormRespondEvent(playerFile, kitsManager, ffaConfig, this, roomConfig, battleBasis, mainForm, kitForm), this);

        // Register Commands

        this.getServer().getCommandMap().register("ffa", new FFACommand(mainForm));
        this.getServer().getCommandMap().register("ffaadmin", new FFAAdminCommand(mainForm, this));
    }

    /**
     * Retrieves the prefix string used for formatting messages or outputs.
     *
     * @return the current prefix string
     */
    public static String getPrefix() {
        return prefix;
    }
}
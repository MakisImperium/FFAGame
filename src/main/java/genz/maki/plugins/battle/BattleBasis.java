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

    public void joinToBattle(Player player) {
        if(this.worldUtils.isWorldLoaded()) {
            Level level = this.main.getServer().getLevelByName(WORLDNAME);
            player.teleport(level.getSafeSpawn());

            player.sendMessage(TextFormat.GREEN + "You have joined the battle!");

            this.roomConfig.addPlayerToRoom(player);
            this.playerFile.createPlayerFile(player);

            giveItemsToBattle(player);
        }
    }

    public void giveItemsToBattle(Player player) {

        player.getInventory().clearAll();

        Item sword = new Item(Item.DIAMOND_SWORD, 0, 1);
        sword.setUnbreakable(true);
        Item apple = new Item(Item.GOLDEN_APPLE, 0, 3);
        Item diamondhelmet = new Item(Item.DIAMOND_HELMET, 0, 1).setUnbreakable(true);
        Item diamondchestplate = new Item(Item.DIAMOND_CHESTPLATE, 0, 1).setUnbreakable(true);
        Item diamondlegs = new Item(Item.DIAMOND_LEGGINGS, 0, 1).setUnbreakable(true);
        Item diamondboots = new Item(Item.DIAMOND_BOOTS, 0, 1).setUnbreakable(true);

        player.getInventory().addItem(sword);
        player.getInventory().addItem(apple);

        player.getInventory().setHelmet(diamondhelmet);
        player.getInventory().setChestplate(diamondchestplate);
        player.getInventory().setLeggings(diamondlegs);
        player.getInventory().setBoots(diamondboots);

        player.setMaxHealth(20);
        player.setFoodEnabled(false);
    }

    public void leaveBattle(Player player) {
        player.getInventory().clearAll();

        this.roomConfig.removePlayerFromRoom(player);

        player.teleport(this.main.getServer().getDefaultLevel().getSafeSpawn());
        player.sendMessage(TextFormat.RED + "You have left the battle!");
    }
}

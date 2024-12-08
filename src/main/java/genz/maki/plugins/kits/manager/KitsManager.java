package genz.maki.plugins.kits.manager;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.battle.BattleBasis;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class KitsManager {

    private final Main plugin;
    private final BattleBasis battleBasis;
    private final String kitsFolder;
    private final List<String> playersEditList;
    private final Map<String, String> playersChoosenKit;
    
    // Player with Item Name
    private final Map<String, String> playersEditItemKit;

    // Player with Kit Name
    private final Map<String, String> playersEditItemKitName;

    public KitsManager(Main plugin, BattleBasis battleBasis) {
        this.plugin = plugin;
        this.battleBasis = battleBasis;
        this.kitsFolder = plugin.getDataFolder() + "/kits";
        playersChoosenKit = new HashMap<>();
        playersEditItemKit = new HashMap<>();
        playersEditList = new ArrayList<>();
        playersEditItemKitName = new HashMap<>();
        createKitsFolderAndStandartKitFile();
    }

    /**
     * Creates the kits folder and a default standard kit file if they do not already exist.
     * The folder is created at the location specified by the kitsFolder field.
     * A template kit file named "Standartkit.txt" is created inside the kits folder
     * with predefined items and armor if it does not already exist.
     *
     * The template file includes:
     * - A diamond sword
     * - A diamond axe
     * - A golden apple
     * - A set of diamond armor (helmet, chestplate, leggings, boots)
     *
     * Any I/O exceptions that occur while creating the file or directory are logged as errors.
     */
    // Funktion zum Erstellen des Kits-Ordners und der Vorlage
    private void createKitsFolderAndStandartKitFile() {
        File folder = new File(kitsFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File templateFile = new File(kitsFolder + "/Standartkit.txt");
        if (!templateFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(templateFile))) {
                List<Integer> items = new ArrayList<>();

                items.add(ItemID.DIAMOND_SWORD);
                items.add(ItemID.DIAMOND_AXE);
                items.add(ItemID.GOLDEN_APPLE);

                writer.write("name: Standard Kit\n");
                writer.write("items: " + items + "\n");
                writer.write("helmet: " + ItemID.DIAMOND_HELMET + "\n");
                writer.write("chestplate: " + ItemID.DIAMOND_HELMET + "\n");
                writer.write("leggins: " + ItemID.DIAMOND_LEGGINGS + "\n");
                writer.write("boots: " + ItemID.DIAMOND_BOOTS + "\n");
            } catch (IOException e) {
                plugin.getLogger().error("Failed to create the template kit file: " + e.getMessage());
            }
        }
    } // Funktion zum Erstellen neuer Kit-Dateien mit der Vorlage

    /**
     * Creates a new kit file based on the player's current inventory and assigns
     * the specified kit name to it. The method stores the player's item IDs
     * in a text file without including the equipped armor pieces.
     *
     * @param kitName the name of the new kit to be created. This will be used as the
     *                filename and as the identifier within the file.
     * @param player the player whose current inventory and armor items will be
     *               saved into the new kit file. The relevant item IDs are extracted
     *               from the player's inventory and written to the text file.
     */
    public void createNewKitFile(String kitName, Player player) {
        File newKitFile = new File(kitsFolder + "/" + kitName + ".txt");
        if (!newKitFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(newKitFile))) {

                List<Integer> items = player.getInventory().getContents().values().stream()
                        .map(Item::getId)
                        .collect(Collectors.toList());

                items.removeIf(id -> id.equals(player.getInventory().getHelmet().getId()) ||
                        id.equals(player.getInventory().getChestplate().getId()) ||
                        id.equals(player.getInventory().getLeggings().getId()) ||
                        id.equals(player.getInventory().getBoots().getId()));

                writer.write("name: " + kitName + "\n");
                writer.write("items: " + items + "\n");
                writer.write("helmet: " + player.getInventory().getHelmet().getId() + "\n");
                writer.write("chestplate: " + player.getInventory().getChestplate().getId() + "\n");
                writer.write("leggins: " + player.getInventory().getLeggings().getId() + "\n");
                writer.write("boots: " + player.getInventory().getBoots().getId() + "\n");
                plugin.getLogger().info("New kit file created: " + kitName + ".txt");
            } catch (IOException e) {
                plugin.getLogger().error("Failed to create new kit file: " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("Kit file already exists: " + kitName + ".txt");
        }
    } // Funktion zum Auflisten aller Dateien im kits-Ordner

    /**
     * Lists all the kit files available in the kits folder, excluding the file extension.
     * Only files with a ".txt" extension are considered kit files.
     *
     * @return a list of kit file names without the ".txt" extension found in the kits folder
     */
    public List<String> listKitFiles() {
        List<String> kitFiles = new ArrayList<>();
        File folder = new File(kitsFolder);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    kitFiles.add(file.getName().replace(".txt", ""));
                }
            }
        }
        return kitFiles;
    }


    /**
     * Loads and applies a specified kit to the given player. This function will read the kit
     * configuration from a file, clear the player's current inventory, and apply armor and
     * items specified in the kit. If the kit file does not exist or an error occurs during
     * application, it logs a warning or error message.
     *
     * @param player  the player to whom the kit will be applied
     * @param kitName the name of the kit to be applied
     */
    public void applyKit(Player player, String kitName) {
        File kitFile = new File(kitsFolder, kitName + ".txt");
        if (!kitFile.exists()) {
            plugin.getLogger().warning("Kit file does not exist: " + kitName + ".txt");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(kitFile))) {
            Map<String, String> kitContents = reader.lines()
                    .map(line -> line.split(": ", 2))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));

            player.getInventory().clearAll();

            kitContents.entrySet().stream()
                    .filter(entry -> List.of("helmet", "chestplate", "leggings", "boots").contains(entry.getKey()))
                    .forEach(entry -> {
                        switch (entry.getKey()) {
                            case "helmet":
                                player.getInventory().setHelmet(Item.get(Integer.parseInt(entry.getValue())));
                                break;
                            case "chestplate":
                                player.getInventory().setChestplate(Item.get(Integer.parseInt(entry.getValue())));
                                break;
                            case "leggings":
                                player.getInventory().setLeggings(Item.get(Integer.parseInt(entry.getValue())));
                                break;
                            case "boots":
                                player.getInventory().setBoots(Item.get(Integer.parseInt(entry.getValue())));
                                break;
                        }
                    });

            if (kitContents.containsKey("items") && !"null".equals(kitContents.get("items"))) {
                String[] items = kitContents.get("items").split(",");
                for (String itemString : items) {
                    Item item = Item.fromString(itemString.trim());
                    player.getInventory().addItem(item);
                }
            }

            addPlayerToKitList(player, kitContents.get("name"));

            player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "You have received the " + kitContents.get("name") + " kit!");
        } catch (IOException e) {
            plugin.getLogger().error("Failed to apply kit: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of item IDs that are part of the specified kit.
     *
     * @param kitName the name of the kit for which item IDs should be retrieved.
     *                This name corresponds to a file that contains the kit details.
     * @return a list of integers representing the item IDs in the specified kit.
     *         If the kit file does not exist or no items are found, an empty list is returned.
     */
    public List<Integer> getKitItems(String kitName) {
        List<Integer> itemsList = new ArrayList<>();
        File kitFile = new File(kitsFolder, kitName + ".txt");
        if (!kitFile.exists()) {
            plugin.getLogger().warning("Kit file does not exist: " + kitName + ".txt");
            return itemsList;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(kitFile))) {
            Map<String, Object> kitContents = reader.lines()
                    .map(line -> line.split(": ", 2))
                    .filter(parts -> parts.length == 2) // Filter out lines that don't have exactly 2 parts
                    .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> {
                        String value = parts[1].trim();
                        try {
                            return Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            return value;
                        }
                    }));

            List<String> keys = List.of("items", "helmet", "chestplate", "leggings", "boots");

            for (String key : keys) {
                if (kitContents.containsKey("items")) {
                    if ("null".equals(kitContents.get("items"))) {
                        plugin.getLogger().emergency("Kit file contains null value for key: items in file: " + kitName + ".txt");
                    } else {
                        Object itemsObject = kitContents.get("items");
                        if (itemsObject instanceof String) {
                            String itemsString = ((String) itemsObject);
                            if (itemsString.startsWith("[") && itemsString.endsWith("]")) {
                                itemsString = itemsString.substring(1, itemsString.length() - 1); // Remove the enclosing brackets
                            }
                            String[] items = itemsString.split(",");
                            for (String itemString : items) {
                                try {
                                    int item = Integer.parseInt(itemString.trim());
                                    if (!itemsList.contains(item)) {
                                        itemsList.add(item);
                                    }
                                } catch (NumberFormatException e) {
                                    plugin.getLogger().warning("Invalid item format: " + itemString);
                                }
                            }
                        }
                    }
                }
                List<String> armorKeys = List.of("helmet", "chestplate", "leggings", "boots");
                for (String armorKey : armorKeys) {
                    Object armorValue = kitContents.get(armorKey);
                    if (armorValue instanceof Integer && !itemsList.contains(armorValue)) {
                        itemsList.add((Integer) armorValue);
                    }
                }

            }
        } catch (IOException e) {
            plugin.getLogger().error("Failed to read items from kit: " + e.getMessage());
        }
        return itemsList;
    }


    /**
     * Updates the items in a specified kit by comparing them with the items present in the player's inventory,
     * and adds any new items to the kit that are not already present.
     *
     * @param kitName the name of the kit that is being modified
     * @param player the player whose inventory is used to update the kit items
     */
    public void changeKitItems(String kitName, Player player) {
        List<Integer> kitItems = getKitItems(kitName);
        List<String> addedItems = new ArrayList<>();

        // Get all items from the player's inventory
        List<Integer> playerItems = player.getInventory().getContents().values().stream()
                .map(Item::getId)
                .toList();

        // Add new items that are not in the existing list
        playerItems.stream()
                .filter(itemId -> !kitItems.contains(itemId))
                .forEach(itemId -> {
                    kitItems.add(itemId);
                    addedItems.add(String.valueOf(itemId));
                });

        updateKitItems(kitName, player, kitItems);
        player.sendMessage(Main.getPrefix() + TextFormat.WHITE + "Added items: " + String.join(", ", addedItems));
    }

    /**
     * Updates the kit items for a specified kit name by writing the player's current inventory items
     * to a corresponding kit file. The method first checks if the kit file exists, then clears existing
     * items in the kit, and adds new items based on the player's inventory.
     *
     * @param kitName  the name of the kit to be updated
     * @param player   the player whose inventory is used to update the kit items
     * @param kitItems a list of existing kit items, which is modified to reflect the player's current inventory
     */
    private void updateKitItems(String kitName, Player player, List<Integer> kitItems) {
        File kitFile = new File(kitsFolder, kitName + ".txt");
        if (!kitFile.exists()) {
            plugin.getLogger().warning("Kit file does not exist: " + kitName + ".txt");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(kitFile))) {
            Map<String, String> kitContents = reader.lines()
                    .map(line -> line.split(": ", 2))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));

            // Clear all existing items
            kitContents.put("items", "");

            // Get player's current items & armor
            
            List<Integer> playerItemIds = new ArrayList<>();
            player.getInventory().getContents().values().forEach(item -> playerItemIds.add(item.getId()));

            List<Integer> armor = new ArrayList<>();

            armor.add(player.getInventory().getHelmet().getId());
            armor.add(player.getInventory().getChestplate().getId());
            armor.add(player.getInventory().getLeggings().getId());
            armor.add(player.getInventory().getBoots().getId());

            // Check and remove elements from playerItemIds if they exist in armor
            armor.forEach(id -> {
                if (playerItemIds.contains(id)) {
                    playerItemIds.remove(id);
                }
            });

            // Remove the last item if present
            List<String> distinctItemIds = playerItemIds.stream()
                    .distinct()
                    .map(String::valueOf)
                    .collect(Collectors.toCollection(ArrayList::new));

            // Avoid trailing comma by joining the list correctly
            kitContents.put("items", String.join(",", distinctItemIds));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(kitFile))) {
                writer.write("name: " + kitName + "\n");
                writer.write("items: " + kitContents.get("items") + "\n");
                writer.write("helmet: " + player.getInventory().getHelmet().getId() + "\n");
                writer.write("chestplate: " + player.getInventory().getChestplate().getId() + "\n");
                writer.write("leggings: " + player.getInventory().getLeggings().getId() + "\n");
                writer.write("boots: " + player.getInventory().getBoots().getId() + "\n");
                writer.write("\n");
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        plugin.getLogger().info("Kit items updated: " + kitName);
    }

    /**
     * Removes a specified item from a kit, identified by its name.
     *
     * @param player   the player requesting the item removal
     * @param itemName the name of the item to be removed from the kit
     * @param kitName  the name of the kit from which the item should be removed
     */
    public void removeItemFromKit(Player player, String kitName, String itemName) {
        List<Integer> kitItems = getKitItems(kitName);
        Item item = Item.fromString(itemName);
        int itemId = item.getId();

        plugin.getLogger().info("Item " + itemName + " found in kit " + kitName + ".");

        if (kitItems.remove((Integer) itemId)) {
            List<String> keys = List.of("items", "helmet", "chestplate", "leggings", "boots");
            File kitFile = new File(kitsFolder, kitName + ".txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(kitFile))) {
                Map<String, String> kitContents = reader.lines()
                        .map(line -> line.split(": ", 2))
                        .filter(parts -> parts.length == 2)
                        .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));

                for (String key : keys) {
                    if (kitContents.containsKey(key)) {
                        if ("items".equals(key)) {
                            String items = kitContents.get(key);
                            List<Integer> itemList = new ArrayList<>(List.of(Arrays.asList(items.split(",")).stream().map(String::trim).map(Integer::parseInt).toArray(Integer[]::new)));
                            itemList.remove((Integer) itemId);
                            kitContents.put(key, itemList.toString().replaceAll("[\\[\\] ]", ""));
                        } else {
                            if (Integer.parseInt(kitContents.get(key).trim()) == itemId) {
                                kitContents.put(key, "0");
                            }
                        }
                    }
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(kitFile))) {
                    for (Map.Entry<String, String> entry : kitContents.entrySet()) {
                        writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                    }
                }

                plugin.getLogger().info("Item " + itemName + " removed from kit " + kitName + ".");
                player.sendMessage(plugin.getPrefix() + TextFormat.RED + "Item: " + getPlayersEditItem(player) + " has been removed from " + getPlayerKitName(player) + " kit.");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to remove item from kit: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("Item not present in kit.");
        }
    }

    /**
     * Adds a player to the edit list if they are not already present in it.
     *
     * @param player the player to be added to the edit list.
     */
    public void addPlayerToList(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!playersEditList.contains(playerId)) {
            playersEditList.add(playerId);
        }
    }

    /**
     * Removes a kit file identified by the specified kit name and notifies the player of the result.
     * If the kit file exists, it will be deleted and the player will be informed of the successful deletion.
     * If the kit file does not exist, a warning will be logged indicating the failure to find the specified kit file.
     *
     * @param kitName the name of the kit to be removed
     * @param player the player who initiated the kit removal
     */
    public void removeKit(String kitName, Player player) {
        File kitFile = new File(kitsFolder, kitName + ".txt");
        if (kitFile.exists()) {
            kitFile.delete();
            player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Kit " + kitName + " deleted!");
            plugin.getLogger().info("Kit file deleted: " + kitName + ".txt");
        } else {
            plugin.getLogger().warning("Kit cannot removed because file does not exist: " + kitName + ".txt");
        }
    }

    /**
     * Checks if the given player is in the list of players allowed to edit kits.
     *
     * @param player the player whose presence in the edit list is being checked
     * @return true if the player is in the edit list, false otherwise
     */
    public boolean isPlayerInEditList(Player player) {
        return playersEditList.contains(player.getUniqueId().toString());
    }

    /**
     * Removes a player from the players edit list.
     *
     * @param player the Player object representing the player to be removed from the list
     */
    public void removePlayerFromList(Player player) {
        playersEditList.remove(player.getUniqueId().toString());
    }

    /**
     * Adds a player to the list of players who have a chosen kit.
     *
     * @param player The player to be added to the kit list.
     * @param kitName The name of the kit that the player has chosen.
     */
    public void addPlayerToKitList(Player player, String kitName) {
        this.playersChoosenKit.put(player.getUniqueId().toString(), kitName);
    }

    /**
     * Removes a player from the kit list, effectively disassociating them from any currently chosen kit.
     *
     * @param player The player to remove from the kit list. The player's unique identifier (UUID) is used
     *               to reference their entry in the kit list.
     */
    public void removePlayerFromKitList(Player player) {
        this.playersChoosenKit.remove(player.getUniqueId().toString());
    }

    /**
     * Checks if a player is in the list of players who have chosen a kit.
     *
     * @param player The player whose presence in the kit list is to be checked.
     * @return true if the player is in the kit list, false otherwise.
     */
    public boolean isPlayerInKitList(Player player) {
        return this.playersChoosenKit.containsKey(player.getUniqueId().toString());
    }

    /**
     * Changes the kit associated with a given player to a new kit specified by the name provided.
     * This method updates the player's chosen kit in the internal mapping.
     *
     * @param player The player whose kit is to be changed.
     * @param newKitName The name of the new kit to assign to the player.
     */
    public void changePlayerKitFromList(Player player, String newKitName) {
        if (this.playersChoosenKit.containsKey(player.getUniqueId().toString())) {
            this.playersChoosenKit.put(player.getUniqueId().toString(), newKitName);
        }
    }

    /**
     * Retrieves the kit currently selected by the given player.
     *
     * @param player the player whose selected kit is to be retrieved
     * @return the name of the kit currently selected by the player, or null if no kit is selected
     */
    public String getPlayerKit(Player player) {
        return this.playersChoosenKit.get(player.getUniqueId().toString());
    }

    /**
     * Retrieves the editing item associated with the specified player.
     *
     * @param player the Player object representing the player whose edit item is to be retrieved.
     * @return a String representing the edit item of the player, or null if no item is associated.
     */
    public String getPlayersEditItem(Player player) {
        return playersEditItemKit.get(player.getUniqueId().toString());
    }

    /**
     * Assigns a specific item to a player for editing, storing the mapping between
     * the player's unique identifier and the item in an internal map.
     *
     * @param player the Player object representing the player who is editing the item.
     * @param item the item assigned to the player for editing.
     */
    public void addPlayersEditItem(Player player, String item) {
        playersEditItemKit.put(player.getUniqueId().toString(), item);
    }

    /**
     * Removes a player from the list of players editing items.
     *
     * @param player the Player object representing the player to be removed from the edit item list.
     *               The player's unique identifier (UUID) is used to reference their entry in the list.
     */
    public void removePlayersEditItem(Player player) {
        playersEditItemKit.remove(player.getUniqueId().toString());
    }

    /**
     * Retrieves the kit name associated with the specified player.
     *
     * @param player the Player object representing the player whose kit name is to be retrieved.
     * @return the name of the kit associated with the player, or null if no kit name is associated with the player.
     */
    public String getPlayerKitName(Player player) {
        return playersEditItemKitName.get(player.getUniqueId().toString());
    }

    /**
     * Associates a kit name with the specified player. This method updates the internal mapping
     * to reflect the kit name chosen or associated with the player.
     *
     * @param player the Player object representing the individual whose kit name is to be set or updated.
     * @param kitName the name of the kit to be associated with the player.
     */
    public void addPlayerKitName(Player player, String kitName) {
        playersEditItemKitName.put(player.getUniqueId().toString(), kitName);
    }

    /**
     * Removes the kit name associated with a specified player from the internal mapping,
     * effectively disassociating the player from any currently selected kit name.
     *
     * @param player the Player object representing the player whose kit name is to be removed.
     *               The player's unique identifier (UUID) is used to manage their entry in the map.
     */
    public void removePlayerKitName(Player player) {
        playersEditItemKitName.remove(player.getUniqueId().toString());
    }

}

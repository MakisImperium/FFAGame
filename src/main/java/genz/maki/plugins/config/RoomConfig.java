package genz.maki.plugins.config;

import cn.nukkit.Player;
import genz.maki.plugins.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomConfig {

    /**
     * Represents the main plugin instance used for managing and accessing
     * various configuration and utility classes associated with the room
     * configuration.
     */
    private final Main plugin;
    /**
     * Represents the file path used for storing or retrieving room configuration data.
     * This file path is intended to point to the location where the configuration
     * file for the room settings is saved, allowing for data storage and retrieval
     * operations within the application.
     */
    private final String FILEPATH;

    /**
     * Constructs a RoomConfig instance.
     *
     * @param plugin The Main class instance that provides plugin-specific functionality and configuration access.
     */
    public RoomConfig(Main plugin) {
        this.plugin = plugin;
        this.FILEPATH = plugin.getDataFolder() + "/room/chache.txt";
    }

    /**
     * Creates a cache file at the specified file path location.
     * If the file does not already exist, it is created and a message is logged indicating that the cache file
     * has been successfully created.
     * If the file already exists, a message is printed to the console indicating that the cache is being loaded.
     * In the event of an IOException during the file creation or writing process, an error message is printed
     * and the stack trace of the exception is output to the console.
     */
    public void createChacheFile() {
        try {
            File myObj = new File(this.FILEPATH);
            if (myObj.createNewFile()) {
                FileWriter myWriter = getFileWriter();
                myWriter.close();

                plugin.getLogger().notice("chache.txt has been created!");
            } else {
                plugin.getLogger().info("Chache loading.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a FileWriter instance for writing data to a specified file path.
     * This method initializes a data map with a specific key-value pair and writes
     * the entries of this map to the file associated with the FileWriter.
     *
     * @return a FileWriter instance used to write data to the designated file.
     * @throws IOException if an I/O error occurs when creating or writing to the file.
     */
    private FileWriter getFileWriter() throws IOException {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("players-playing", new ArrayList<>());
        FileWriter myWriter = new FileWriter(this.FILEPATH);
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            myWriter.write(entry.getKey() + entry.getValue() + "\n");
        }
        return myWriter;
    }

    /**
     * Saves a HashMap with keys and associated list of strings to a file.
     * Each entry is written in the format 'key: value1, value2, ...'.
     *
     * @param map the HashMap to be saved to the file. The key is a String, and the value is a list of Strings.
     * @throws IOException if an I/O error occurs while writing to the file.
     */
    public void saveHashMapToFile(HashMap<String, List<String>> map) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.FILEPATH))) {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                writer.write(entry.getKey() + ": " + String.join(", ", entry.getValue()));
                writer.newLine();
            }
        }
    }
    /**
     * Loads a HashMap of Strings to List of Strings from a file specified by the FILEPATH.
     * Each line in the file should be in the format: "key: value1, value2, value3".
     * The method reads the file line by line, splits each line by a colon to separate the key
     * from the values, and then splits the values by a comma to form the list of strings.
     * It populates and returns the HashMap constructed from the file contents.
     *
     * @return A HashMap where each key is associated with a list of strings read from the file.
     * @throws IOException If there is an error in opening, reading, or closing the file.
     */
    // Methode zum Einlesen einer HashMap mit ArrayLists aus einer Datei
    public HashMap<String, List<String>> loadHashMapFromFile() throws IOException {
        HashMap<String, List<String>> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(this.FILEPATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String[] values = parts[1].trim().split(", ");
                    List<String> valueList = new ArrayList<>();
                    for (String value : values) {
                        if (!value.isEmpty()) {
                            valueList.add(value);
                        }
                    }
                    map.put(key, valueList);
                }
            }
        }
        return map;
    }

    /**
     * Adds a player to the room by updating the internal player list and saving changes to the file.
     *
     * @param player the player to be added to the room, identified by their unique ID
     */
    public void addPlayerToRoom(Player player) {
        HashMap<String, List<String>> map = new HashMap<>();
        try {
            map = loadHashMapFromFile();
        } catch (IOException e) {
            plugin.getLogger().error("An error occurred. Cache.txt cannot be loaded. Action: addPlayerToRoom");
        }
        String playerToAdd = player.getUniqueId().toString();
        map.computeIfAbsent("players-playing", k -> new ArrayList<>()).add(playerToAdd);
        try {
            saveHashMapToFile(map);
        } catch (IOException e) {
            plugin.getLogger().error("Error saving the file: " + e.getMessage());
        }
    }

    /**
     * Removes a specified player from the room.
     * This method updates the list of players currently in the room by
     * removing the specified player's unique identifier.
     * It then saves the updated list back to the file storage.
     *
     * @param player the player to be removed from the room. The player's unique identifier is used
     *               to identify and remove them from the list of active players in the room.
     */
    public void removePlayerFromRoom(Player player) {
        HashMap<String, List<String>> map = new HashMap<>();
        try {
            map = loadHashMapFromFile();
        } catch (IOException e) {
            plugin.getLogger().error("An error occurred. Cache.txt cannot be loaded. Action: removePlayerFromRoom");
        }
        String playerToRemove = player.getUniqueId().toString();
        List<String> players = map.get("players-playing");
        if (players != null && players.contains(playerToRemove)) {
            players.remove(playerToRemove);
        }
        map.put("players-playing", players); // Update the list, even if it is empty
        try {
            saveHashMapToFile(map);
        } catch (IOException e) {
            plugin.getLogger().error("Error saving the file: " + e.getMessage());
        }
}

    /**
     * Counts the number of players currently playing from the given map.
     *
     * @param map a HashMap where the key "players-playing" corresponds to a list of players
     * @return the count of players in the list associated with the key "players-playing", or 0 if the key does not exist
     */
    public int countPlayers(HashMap<String, List<String>> map) {
        if (map.containsKey("players-playing")) {
            return map.get("players-playing").size();
        } else {
            return 0;
        }
    }

    /**
     * Checks whether a given player is in the "players-playing" list.
     *
     * @param player The player to check for membership in the "players-playing" list.
     * @return true if the player is in the "players-playing" list, otherwise false.
     * @throws IOException If an I/O error occurs while loading the hash map from the file.
     */
    public boolean isPlayerInList(Player player) throws IOException {
        HashMap<String, List<String>> map = loadHashMapFromFile();
        if (map.containsKey("players-playing")) {
            return map.get("players-playing").contains(player.getUniqueId().toString());
        } else {
            return false;
        }
    }
}

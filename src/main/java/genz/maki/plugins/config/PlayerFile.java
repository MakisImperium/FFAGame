package genz.maki.plugins.config;

import cn.nukkit.Player;
import genz.maki.plugins.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerFile {

    private final Main main;

    public PlayerFile(Main main) {
        this.main = main;
    }

    /**
     * Creates a new player file if it doesn't already exist and initializes it with player data.
     *
     * @param player The player for whom the file is being created. The player's unique ID is used
     *               to generate the filename, and initial player data (name, UUID, kills, deaths, XP)
     *               is written to the file.
     */
    public void createPlayerFile(Player player) {
        try {
            File myObj = new File(main.getDataFolder() + "/players/", player.getUniqueId().toString() + ".txt");
            if (myObj.createNewFile()) {
                FileWriter myWriter = getFileWriter(player);
                myWriter.close();

            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Checks if a player file already exists.
     *
     * @param player The player whose file existence is being checked.
     * @return true if the player's file exists, false otherwise.
     */
    public boolean doesPlayerFileExist(Player player) {
        File myObj = new File(main.getDataFolder() + "/players/", player.getUniqueId().toString() + ".txt");
        return myObj.exists();
    }

    /**
     * Generates a FileWriter for a given player. Writes initial player data to a file.
     *
     * @param player the player whose data will be written to the file
     * @return a FileWriter object with the player's data written into a text file
     * @throws IOException if an I/O error occurs
     */
    private FileWriter getFileWriter(Player player) throws IOException {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("Name", player.getName());
        dataMap.put("UUID", player.getUniqueId().toString());
        dataMap.put("Kills", 0); // Initialize Kills as Integer
        dataMap.put("Deaths", 0);
        dataMap.put("XP", 0); // Initialize XP as Integer
        FileWriter myWriter = new FileWriter(main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt");
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            myWriter.write(entry.getKey() + ": " + entry.getValue() + "\n");
        }
        return myWriter;
    }

    /**
     * Reads the content of a player's file and returns it as a string.
     *
     * @param player The player whose file content is to be read. The player's unique ID
     *               determines the file name.
     * @return A string representing the content of the player's file. Each line of the file
     * is separated by a newline character in the returned string.
     * @throws RuntimeException If an I/O error occurs while reading the file.
     */
    public String getPlayerFile(Player player) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    /**
     * Reads a file and converts its contents into a HashMap where each line
     * represents a key-value pair separated by a colon followed by a space.
     *
     * @param filePath the path to the file that will be read and converted into a HashMap
     * @return a HashMap containing key-value pairs extracted from the file
     * @throws IOException if there is an error reading the file
     */
    public HashMap<String, Object> fileToHashMap(String filePath) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    switch (key) {
                        case "Kills", "Deaths":
                            map.put(key, Integer.parseInt(value));
                            break;
                        case "XP":
                            map.put(key, Integer.parseInt(value));
                            break;
                        default:
                            map.put(key, value);
                            break;
                    }
                }
            }
        }
        return map;
    }


    /**
     * Updates the specified key in the player's HashMap with a new value and writes it back to the file.
     *
     * @param player   the player whose data is being updated
     * @param key      the key in the HashMap to be updated
     * @param newValue the new value to associate with the specified key
     * @throws IOException if an I/O error occurs while reading from or writing to the file
     */
    public void updateValueInHashMap(Player player, String key, Object newValue) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        HashMap<String, Object> map = fileToHashMap(filePath);
        map.put(key, newValue); // Schreibe die aktualisierte HashMap zurück in die Datei
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }
    }

    /**
     * Retrieves the value associated with the specified key from a player's data file.
     *
     * @param player The player whose data file is being accessed.
     * @param key    The key whose associated value is to be returned.
     * @return The value associated with the specified key, or null if the key is not present in the data file.
     * @throws IOException If there is an error reading the player's data file.
     */
    public Object getValueFromHashMap(Player player, String key) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        HashMap<String, Object> map = fileToHashMap(filePath);
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            main.getLogger().error("Unable to determine a specific key value: " + key);
            return 0; // Return a default value to avoid NullPointerException
        }
    }

    /**
     * Retrieves the integer value associated with the specified key from a player's data file.
     *
     * @param player The player whose data file is being accessed.
     * @param key    The key whose associated integer value is to be returned.
     * @return The integer value associated with the specified key, or 0 if the key is not present or not an integer.
     * @throws IOException If there is an error reading the player's data file.
     */
    public Integer getIntegerValueFromHashMap(Player player, String key) throws IOException {
        Object value = getValueFromHashMap(player, key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                main.getLogger().error("Error parsing value for key " + key + ": " + value);
            }
        } else {
            main.getLogger().error("Value for key " + key + " is not an Integer or String: " + value);
        }
        return 0; // Return 0 to avoid NullPointerException
    }

    /**
     * Increments the kill count for a specified player by one.
     *
     * @param player the player whose kill count is to be incremented
     * @throws IOException if there is an error accessing the player's data file
     */
    public void addKill(Player player) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        Object current = getValueFromHashMap(player, "Kills");

        if (current instanceof Integer) {
            int i = Integer.parseInt(current.toString());
            this.updateValueInHashMap(player,
                    "Kills",
                    i + 1);
        } else {
            main.getLogger().error("Critital Error: Kills Key not equals a Integer\n File: " + filePath);
        }

    }

    /**
     * Increments the death count for the specified player by 1.
     * If the current value of the "Deaths" key in the player's data is not an integer,
     * an error will be logged.
     *
     * @param player The player whose death count is to be incremented.
     * @throws IOException If an I/O error occurs while accessing or updating the player's data file.
     */
    public void addDeath(Player player) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        Object current = getValueFromHashMap(player, "Deaths");

        if (current instanceof Integer) {
            int i = Integer.parseInt(current.toString());
            this.updateValueInHashMap(player,
                    "Deaths",
                    i + 1);
        } else {
            main.getLogger().error("Critital Error: Deaths Key not equals a Integer\n File: " + filePath);
        }

    }

    /**
     * Retrieves the number of kills for a specified player.
     *
     * @param player The player whose kill count is to be retrieved.
     * @return The number of kills recorded for the player. Returns 0 if the data is not a valid integer or on error.
     * @throws IOException If an I/O error occurs while accessing the player's data.
     */
    public int getKills(Player player) throws IOException {
        Object kills = getValueFromHashMap(player, "Kills");
        if (kills != null) {
            if (kills instanceof String) {
                try {
                    return Integer.parseInt((String) kills);
                } catch (NumberFormatException e) {
                    main.getLogger().error("Critical Error: Kills Key is not a valid Integer: " + kills);
                    return 0;
                }
            } else if (kills instanceof Integer) {
                return (Integer) kills;
            } else {
                main.getLogger().error("Critical Error: Kills Key is not an Integer or String, Type is: " + kills.getClass().getName());
                return 0;
            }
        } else {
            main.getLogger().info("Kills Key not found in player's data. Initializing to 0.");
            updateValueInHashMap(player, "Kills", 0);
            return 0;
        }
    }

    /**
     * Retrieves the number of deaths for a given player.
     *
     * @param player The player for whom to retrieve the death count.
     * @return The number of deaths recorded for the player. Returns 0 if the data is not an integer or on error.
     * @throws IOException If an input or output exception occurs while retrieving the data.
     */
    public int getDeaths(Player player) throws IOException {
        Integer deaths = getIntegerValueFromHashMap(player, "Deaths");
        if (deaths == null) {
            main.getLogger().error("Critical Error: Deaths Key not found or not an Integer. Returning default value 0.");
            return 0;
        }
        return deaths;
    }

    /**
     * Calculates the kill/death ratio for a given player.
     * The method retrieves the number of kills and deaths from the player's data
     * and computes the ratio as kills divided by deaths. If there are no deaths,
     * the method returns the number of kills as the ratio.
     *
     * @param player The player for whom the kill/death ratio is to be calculated.
     * @return The kill/death ratio as a double. If the player's deaths are zero, the method returns the kill count.
     * @throws IOException If there is an error accessing the player's data.
     */
    public double getKillDeathRatio(Player player) throws IOException {
        int kills = getKills(player);
        int deaths = getDeaths(player);
        return (deaths == 0) ? kills : (double) kills / deaths;
    }

}


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

    public void createPlayerFile(Player player) {
        try {
            File myObj = new File(main.getDataFolder() + "/players/", player.getUniqueId().toString() + ".txt");
            if (myObj.createNewFile()) {
                FileWriter myWriter = getFileWriter(player);
                myWriter.close();

                main.getLogger().notice(player.getName() + " has been created!");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private FileWriter getFileWriter(Player player) throws IOException {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("Name:", player.getName());
        dataMap.put("UUID:", player.getUniqueId().toString());
        dataMap.put("Kills:", 0);
        dataMap.put("Deaths:", 0);
        dataMap.put("XP:", 0);

        FileWriter myWriter = new FileWriter(main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt");
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            myWriter.write(entry.getKey() + entry.getValue() + "\n");
        }
        return myWriter;
    }

    public String getPlayerFile(Player player) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt"))) {
            String line;
            while ((line = reader.readLine()) != null)
            {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    public HashMap<String, Object> fileToHashMap(String filePath) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ", 2);
                // Annahme: String und Object sind durch ':' getrennt
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }
            return map;
        }
    }

    public void updateValueInHashMap(Player player, String key, Object newValue) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        HashMap<String, Object> map = fileToHashMap(filePath);
        if (map.containsKey(key)) {
            map.put(key, newValue);
        } else {
            main.getLogger().error("Key nicht gefunden: " + key);
            return;
        }
        // Schreibe die aktualisierte HashMap zurück in die Datei
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }
    }

    public Object getValueFromHashMap(Player player, String key) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        HashMap<String, Object> map = fileToHashMap(filePath);
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            main.getLogger().error("Konnte kein bestimmter Key Wert ermittelt werden: " + key);
            return null;
        }
    }

    public void addKill(Player player) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        Object current = getValueFromHashMap(player, "Kills");

        if(current instanceof Integer) {
            int i = Integer.parseInt(current.toString());
            this.updateValueInHashMap(player,
                    "Kills",
                    i + 1);
        } else {
            main.getLogger().error("Critital Error: Kills Key not equals a Integer\n File: " + filePath);
        }

    }

    public void addDeath(Player player) throws IOException {
        String filePath = main.getDataFolder() + "/players/" + player.getUniqueId().toString() + ".txt";
        Object current = getValueFromHashMap(player, "Deaths");

        if(current instanceof Integer) {
            int i = Integer.parseInt(current.toString());
            this.updateValueInHashMap(player,
                    "Deaths",
                    i + 1);
        } else {
            main.getLogger().error("Critital Error: Deaths Key not equals a Integer\n File: " + filePath);
        }

    }

    public int getKills(Player player) throws IOException {
        Object kills = getValueFromHashMap(player, "Kills");
        if (kills instanceof Integer) {
            return (Integer) kills;
        } else {
            main.getLogger().error("Critical Error: Kills Key not equals an Integer");
            return 0;
        }
    }

    public int getDeaths(Player player) throws IOException {
        Object deaths = getValueFromHashMap(player, "Deaths");
        if (deaths instanceof Integer) {
            return (Integer) deaths;
        } else {
            main.getLogger().error("Critical Error: Deaths Key not equals an Integer");
            return 0;
        }
    }

    public double getKillDeathRatio(Player player) throws IOException {
        int kills = getKills(player);
        int deaths = getDeaths(player);
        return (deaths == 0) ? kills : (double) kills / deaths;
    }

}


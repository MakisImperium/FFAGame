package genz.maki.plugins.config;

import genz.maki.plugins.Main;

import java.util.Map;

public class FFAConfig {

    private final Main plugin;

    public FFAConfig(Main plugin) {
        this.plugin = plugin;
    }

    public Map<String, Object> getConfiguration() {
        return plugin.getConfig().getAll();
    }

    public String getWorld(){
        if(!(plugin.getConfig().getString("ffa-world") == null)){
            return plugin.getConfig().getString("ffa-world");
        } else {
            plugin.getLogger().emergency("Key: ffa-world in config.yml must not be null.");
            return null;
        }
    }

    public void setWorld(String type){
        plugin.getConfig().set("ffa-world", type);
    }

    public String getWelcomeMessage(){
        return plugin.getConfig().getString("welcome-message");
    }

    public void setWelcomeMessage(String welcomeMessage){ plugin.getConfig().set("welcome-message", welcomeMessage); }
    public Byte getPlayersRespawnDelay(){
        return (Byte) plugin.getConfig().get("playersRespawnDelay");
    }

    public void setPlayersRespawnDelay(Byte playersRespawnDelay){
        plugin.getConfig().set("playersRespawnDelay", playersRespawnDelay);
    }

    public void setFormName(String formName){
        plugin.getConfig().set("form-name", formName);
    }

    public String getFormName(){
        return plugin.getConfig().getString("form-name");
    }

    public void setFormDescription(String formDescription){
        plugin.getConfig().set("form-description", formDescription);
    }

    public String getFormDescription(){
        return plugin.getConfig().getString("form-description");
    }


    // // For Tests only
    //    public void loadConfig(){
    //        try {
    //            File myObj = new File(main.getDataFolder() + "config.yml");
    //            if (myObj.createNewFile()) {
    //                FileWriter myWriter = getFileWriter();
    //                myWriter.close();
    //
    //                main.getLogger().notice("config.yml has been created!");
    //            } else {
    //                System.out.println("FFAConfig loaded.");
    //            }
    //        } catch (IOException e) {
    //            System.out.println("An error occurred.");
    //            e.printStackTrace();
    //        }
    //    }
    //
    //    private FileWriter getFileWriter() throws IOException {
    //        Map<String, Object> dataMap = new HashMap<String, Object>();
    //        dataMap.put("world", "world");
    //        dataMap.put("welcome", "welcome to ffa");
    //        dataMap.put("playersRespawnDelay", 1);
    //        dataMap.put("players", 0);
    //
    //
    //        FileWriter myWriter = new FileWriter(main.getDataFolder() + "config.yml");
    //        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
    //            myWriter.write(entry.getKey() + entry.getValue() + "\n");
    //        }
    //        return myWriter;
    //    }
    //    //

}

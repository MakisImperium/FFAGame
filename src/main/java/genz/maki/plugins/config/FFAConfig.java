package genz.maki.plugins.config;

import genz.maki.plugins.Main;

import java.util.Map;

public class FFAConfig {

    private final Main plugin;

    public FFAConfig(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the complete configuration as a map of key-value pairs.
     *
     * @return a map containing all configuration entries, where keys are the
     *         configuration item names and values are their corresponding settings.
     */
    public Map<String, Object> getConfiguration() {
        return plugin.getConfig().getAll();
    }

    /**
     * Retrieves the configured Free-For-All (FFA) world name from the plugin's configuration.
     * Logs an emergency message if the world name is not set in the configuration.
     *
     * @return the name of the FFA world as a string, or null if the configuration key "ffa-world" is not set.
     */
    public String getWorld(){
        if(!(plugin.getConfig().getString("ffa-world") == null)){
            return plugin.getConfig().getString("ffa-world");
        } else {
            plugin.getLogger().emergency("Key: ffa-world in config.yml must not be null.");
            return null;
        }
    }

    /**
     * Sets the world configuration for free-for-all (FFA) mode.
     *
     * @param type the identifier of the world to be set in the configuration
     */
    public void setWorld(String type){
        plugin.getConfig().set("ffa-world", type);
    }

    /**
     * Retrieves the welcome message from the configuration.
     *
     * @return the welcome message as a String from the configuration file.
     */
    public String getWelcomeMessage(){
        return plugin.getConfig().getString("welcome-message");
    }

    /**
     * Sets the welcome message in the configuration.
     *
     * @param welcomeMessage the welcome message to be set in the configuration
     */
    public void setWelcomeMessage(String welcomeMessage){ plugin.getConfig().set("welcome-message", welcomeMessage); }
    /**
     * Retrieves the respawn delay for players as configured.
     *
     * @return the respawn delay for players in the form of a Byte.
     *         May return null if the configuration is missing this value.
     */
    public Byte getPlayersRespawnDelay(){
        return (Byte) plugin.getConfig().get("playersRespawnDelay");
    }

    /**
     * Sets the players' respawn delay in the configuration.
     *
     * @param playersRespawnDelay the delay in seconds before players respawn
     */
    public void setPlayersRespawnDelay(Byte playersRespawnDelay){
        plugin.getConfig().set("playersRespawnDelay", playersRespawnDelay);
    }

    /**
     * Sets the name of the form in the plugin configuration.
     *
     * @param formName the name to set for the form
     */
    public void setFormName(String formName){
        plugin.getConfig().set("form-name", formName);
    }

    /**
     * Retrieves the name of the form from the configuration.
     *
     * @return the form name as a String, or null if not defined in the configuration
     */
    public String getFormName(){
        return plugin.getConfig().getString("form-name");
    }

    /**
     * Sets the description of the form in the configuration.
     *
     * @param formDescription the description to be set for the form
     */
    public void setFormDescription(String formDescription){
        plugin.getConfig().set("form-description", formDescription);
    }

    /**
     * Retrieves the description text for a form from the configuration.
     *
     * @return A string representing the form's description, as specified in the configuration file.
     *         Returns null if the "form-description" key does not exist in the configuration.
     */
    public String getFormDescription(){
        return plugin.getConfig().getString("form-description");
    }



}

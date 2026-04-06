package makisimperium.ffa.config;

import cn.nukkit.plugin.PluginBase;
import makisimperium.ffa.util.JsonFileService;

import java.io.IOException;
import java.nio.file.Path;

public final class ConfigurationService {

    private final PluginBase plugin;
    private final JsonFileService jsonFileService;
    private final Path configPath;

    public ConfigurationService(PluginBase plugin, JsonFileService jsonFileService) {
        this.plugin = plugin;
        this.jsonFileService = jsonFileService;
        this.configPath = plugin.getDataFolder().toPath().resolve("config.json");
    }

    public PluginConfiguration loadOrCreate() throws IOException {
        PluginConfiguration configuration = jsonFileService.read(configPath, PluginConfiguration.class).orElse(null);
        if (configuration == null) {
            configuration = new PluginConfiguration();
            configuration.normalize();
            jsonFileService.write(configPath, configuration);
            plugin.getLogger().info("Created default config.json");
            return configuration;
        }

        configuration.normalize();
        return configuration;
    }
}



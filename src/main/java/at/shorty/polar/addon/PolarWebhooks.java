package at.shorty.polar.addon;

import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.config.Punishment;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import top.polar.api.loader.LoaderApi;

@Plugin(name = "PolarWebhooks", version = "1.0.1")
@Dependency("PolarLoader")
public class PolarWebhooks extends JavaPlugin {

    @Override
    public void onLoad() {
        saveDefaultConfig();
        YamlConfiguration config = (YamlConfiguration) getConfig();
        Mitigation mitigation = Mitigation.loadFromConfigSection(config.getConfigurationSection("mitigation"));
        Detection detection = Detection.loadFromConfigSection(config.getConfigurationSection("detection"));
        CloudDetection cloudDetection = CloudDetection.loadFromConfigSection(config.getConfigurationSection("cloud_detection"));
        Punishment punishment = Punishment.loadFromConfigSection(config.getConfigurationSection("punishment"));
        LoaderApi.registerEnableCallback(new PolarApiHook(mitigation, detection, cloudDetection, punishment));
    }

}

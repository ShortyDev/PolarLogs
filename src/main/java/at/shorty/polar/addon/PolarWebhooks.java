package at.shorty.polar.addon;

import at.shorty.polar.addon.commands.PolarWebhooksCommand;
import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.config.Punishment;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import top.polar.api.loader.LoaderApi;

import java.lang.reflect.Field;
import java.util.Set;

@Plugin(name = "PolarWebhooks", version = "1.1")
@Dependency("PolarLoader")
public class PolarWebhooks extends JavaPlugin {

    private PolarApiHook polarApiHook;

    @Override
    public void onLoad() {
        updateConfig();
        Mitigation mitigation = Mitigation.loadFromConfigSection(getConfig().getConfigurationSection("mitigation"));
        Detection detection = Detection.loadFromConfigSection(getConfig().getConfigurationSection("detection"));
        CloudDetection cloudDetection = CloudDetection.loadFromConfigSection(getConfig().getConfigurationSection("cloud_detection"));
        Punishment punishment = Punishment.loadFromConfigSection(getConfig().getConfigurationSection("punishment"));
        polarApiHook = new PolarApiHook(mitigation, detection, cloudDetection, punishment);
        LoaderApi.registerEnableCallback(polarApiHook);
    }

    @Override
    public void onEnable() {
        YamlConfiguration config = (YamlConfiguration) getConfig();
        String command = config.getString("command");
        if (!command.equals("disable")) {
            try {
                Field bukkitCommandMap = getServer().getClass().getDeclaredField("commandMap");
                bukkitCommandMap.setAccessible(true);
                CommandMap commandMap = (CommandMap) bukkitCommandMap.get(getServer());
                commandMap.register(command, new PolarWebhooksCommand(this, command));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                getLogger().severe("Failed to register command: " + e.getMessage());
            }
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        polarApiHook.reloadConfig(
                Mitigation.loadFromConfigSection(getConfig().getConfigurationSection("mitigation")),
                Detection.loadFromConfigSection(getConfig().getConfigurationSection("detection")),
                CloudDetection.loadFromConfigSection(getConfig().getConfigurationSection("cloud_detection")),
                Punishment.loadFromConfigSection(getConfig().getConfigurationSection("punishment"))
        );
    }

    // https://www.spigotmc.org/threads/solved-replacing-a-config-file.40420/#post-462207, accessed 1st April 2024
    public void updateConfig() {
        saveDefaultConfig();
        Set<String> options = getConfig().getDefaults().getKeys(false);
        Set<String> currentOptions = getConfig().getKeys(false);
        boolean changed = false;
        for (String option : options) {
            if (!currentOptions.contains(option)) {
                getConfig().set(option, getConfig().getDefaults().get(option));
                changed = true;
            }
        }
        for (String currentOption : currentOptions) {
            if (!options.contains(currentOption)) {
                getConfig().set(currentOption, null);
                changed = true;
            }
        }
        getConfig().options().copyHeader(true);
        if (changed) saveConfig();
    }
}

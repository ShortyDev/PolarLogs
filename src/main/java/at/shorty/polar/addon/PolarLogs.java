package at.shorty.polar.addon;

import at.shorty.polar.addon.commands.PolarLogsCommand;
import at.shorty.polar.addon.config.*;
import lombok.Getter;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import top.polar.api.loader.LoaderApi;

import java.lang.reflect.Field;
import java.util.Set;

@Plugin(name = "PolarLogs", version = "2.2")
@Dependency("PolarLoader")
public class PolarLogs extends JavaPlugin {

    private PolarApiHook polarApiHook;
    @Getter
    private Logs logs;
    public static String prefix = "§bLogs §7| §r";

    @Override
    public void onLoad() {
        updateConfig();
        Mitigation mitigation = Mitigation.loadFromConfigSection(getConfig().getConfigurationSection("mitigation"));
        Detection detection = Detection.loadFromConfigSection(getConfig().getConfigurationSection("detection"));
        CloudDetection cloudDetection = CloudDetection.loadFromConfigSection(getConfig().getConfigurationSection("cloud_detection"));
        Punishment punishment = Punishment.loadFromConfigSection(getConfig().getConfigurationSection("punishment"));
        logs = Logs.loadFromConfigSection(getConfig().getConfigurationSection("logs"));
        polarApiHook = new PolarApiHook(mitigation, detection, cloudDetection, punishment, logs);
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
                PolarLogsCommand polarLogsCommand = new PolarLogsCommand(this, command);
                commandMap.register(command, polarLogsCommand);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                getLogger().severe("Failed to register command: " + e.getMessage());
            }
        }
        loadLogs();
    }

    private void loadLogs() {
        if (logs.isEnabled()) {
            if (!logs.getContext().matches("^[a-zA-Z0-9_]+$") || logs.getContext().isEmpty()) {
                getLogger().severe("Invalid log context name, must be [a-zA-Z0-9_]");
                getLogger().severe("Falling back to default context name: global");
                logs.setContext("global");
            }
            logs.establishConnection().thenAccept(established -> {
                if (established) {
                    getLogger().info("Connected to database.");
                } else {
                    getLogger().severe("Failed to establish connection to database.");
                }
            });
        }
    }

    @Override
    public void onDisable() {
        if (logs != null && logs.isEnabled()) {
            logs.dropConnection();
        }
    }

    public void testWebhook() {
        polarApiHook.testWebhook();
    }

    public void reloadPluginConfig() {
        reloadConfig();
        if (logs != null) {
            logs.dropConnection();
        }
        logs = Logs.loadFromConfigSection(getConfig().getConfigurationSection("logs"));
        loadLogs();
        polarApiHook.reloadConfig(
                Mitigation.loadFromConfigSection(getConfig().getConfigurationSection("mitigation")),
                Detection.loadFromConfigSection(getConfig().getConfigurationSection("detection")),
                CloudDetection.loadFromConfigSection(getConfig().getConfigurationSection("cloud_detection")),
                Punishment.loadFromConfigSection(getConfig().getConfigurationSection("punishment")),
                logs
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

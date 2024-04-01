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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

@Plugin(name = "PolarLogs", version = "2.0")
@Dependency("PolarLoader")
public class PolarLogs extends JavaPlugin {

    private PolarApiHook polarApiHook;
    @Getter
    private Logs logs;
    private Connection connection;

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
                commandMap.register(command, new PolarLogsCommand(this, command));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                getLogger().severe("Failed to register command: " + e.getMessage());
            }
        }
        if (logs.isEnabled()) {
            if (!logs.getContext().matches("^[a-zA-Z0-9_]+$") || logs.getContext().isEmpty()) {
                getLogger().severe("Invalid log context name, must be [a-zA-Z0-9_]");
                getLogger().severe("Using default context name: global");
                logs.setContext("global");
            }
            try {
                String url = "jdbc:mysql://" + logs.getDatabase().getSqlHost() + ":" + logs.getDatabase().getSqlPort() + "/" + logs.getDatabase().getSqlDatabase();
                connection = DriverManager.getConnection(url, logs.getDatabase().getSqlUsername(), logs.getDatabase().getSqlPassword());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            logs.setConnection(connection);
            try {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS polar_logs_" + logs.getContext() + " (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "type VARCHAR(255), " +
                        "player_name VARCHAR(16), " +
                        "player_uuid VARCHAR(36), " +
                        "player_version VARCHAR(20), " +
                        "player_latency INT, " +
                        "player_brand VARCHAR(64), " +
                        "vl DOUBLE, " +
                        "check_type VARCHAR(64), " +
                        "check_name VARCHAR(64), " +
                        "details VARCHAR(1024), " +
                        "punishment_type VARCHAR(16), " +
                        "punishment_reason VARCHAR(64), " +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                statement.execute();
                statement.close();

                PreparedStatement deleteExpiredLogs = connection.prepareStatement("DELETE FROM polar_logs_" + logs.getContext() + " WHERE timestamp < DATE_SUB(NOW(), INTERVAL " + logs.expireAfterDays + " DAY)");
                int updated = deleteExpiredLogs.executeUpdate();
                if (updated > 0) {
                    getLogger().info("Deleted " + updated + " expired logs. (Logs older than " + logs.expireAfterDays + " day(s))");
                }
                deleteExpiredLogs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            getLogger().info("Connected to database.");
        }
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void testWebhook() {
        polarApiHook.testWebhook();
    }

    public void reloadPluginConfig() {
        reloadConfig();
        logs = Logs.loadFromConfigSection(getConfig().getConfigurationSection("logs"));
        logs.setConnection(connection);
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

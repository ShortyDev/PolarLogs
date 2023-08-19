package at.shorty.polar.addon;

import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.config.Punishment;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import top.polar.api.loader.LoaderApi;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


@Plugin(name = "PolarWebhooks", version = "1.0")
@Dependency("PolarLoader")
public class Webhooks extends JavaPlugin {

    @Override
    public void onLoad() {
        saveDefaultConfig();
        YamlConfiguration config = (YamlConfiguration) getConfig();
        Mitigation mitigation = Mitigation.loadFromConfigSection(config.getConfigurationSection("mitigation"));
        Detection detection = Detection.loadFromConfigSection(config.getConfigurationSection("detection"));
        CloudDetection cloudDetection = CloudDetection.loadFromConfigSection(config.getConfigurationSection("cloud_detection"));
        Punishment punishment = Punishment.loadFromConfigSection(config.getConfigurationSection("punishment"));
        LoaderApi.registerEnableCallback(new PolarApiHook(mitigation, detection, cloudDetection, punishment, ExpiringMap.builder().expiration(2, TimeUnit.MINUTES).build()));
    }

    public static void sendWebhook(String webhookUrl, String content) {
        try {
            URL url = new URL(webhookUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);
            connection.getOutputStream().write(content.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.getInputStream().close();
        } catch (IOException e) {
            System.err.println("[PolarWebhooks] Failed to send webhook!");
            e.printStackTrace();
        }
    }

    public static String replacePlaceholders(String input, String playerName, String vl, String punishVl, String checkType, String checkName, String details, String punishment, String reason, String[] detailFilters) {
        if (punishment == null) punishment = "Punishment";
        if (detailFilters != null && detailFilters.length > 0) {
            String[] lines = details.split("\n");
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                if (line.isEmpty()) continue;
                line = ChatColor.translateAlternateColorCodes('&', line);
                line = ChatColor.stripColor(line);
                line = Pattern.compile("<(.*?)>").matcher(line).replaceAll("");
                line = line.trim();
                boolean filter = false;
                for (String filterLine : detailFilters) {
                    if (filterLine.startsWith("*") && filterLine.endsWith("*")) {
                        if (line.contains(filterLine.replace("*", ""))) {
                            filter = true;
                            break;
                        }
                    } else if (filterLine.startsWith("*")) {
                        if (line.endsWith(filterLine.replace("*", ""))) {
                            filter = true;
                            break;
                        }
                    } else if (filterLine.endsWith("*")) {
                        if (line.startsWith(filterLine.replace("*", ""))) {
                            filter = true;
                            break;
                        }
                    } else {
                        if (line.equals(filterLine)) {
                            filter = true;
                            break;
                        }
                    }
                }
                if (!filter) builder.append(line).append("\n");
            }
            details = builder.toString();
        }
        details = details.replace("\n", "\\n").trim();
        reason = reason.replace("\n", "\\n").trim();
        reason = ChatColor.translateAlternateColorCodes('&', reason);
        reason = ChatColor.stripColor(reason);
        reason = Pattern.compile("<(.*?)>").matcher(reason).replaceAll("");
        return input.replace("%PLAYER_NAME%", playerName)
                .replace("%VL%", vl)
                .replace("%PUNISH_VL%", punishVl)
                .replace("%CHECK_TYPE%", checkType)
                .replace("%CHECK_NAME%", checkName)
                .replace("%DETAILS%", details)
                .replace("%PUNISHMENT%", punishment)
                .replace("%REASON%", reason);
    }

}

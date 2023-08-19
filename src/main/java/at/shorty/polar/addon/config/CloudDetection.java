package at.shorty.polar.addon.config;

import com.google.gson.Gson;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import top.polar.api.user.event.type.CloudCheckType;

import java.util.Arrays;

@Data
public class CloudDetection {

    private boolean enabled;
    private int cooldownPerPlayerAndType;
    private String[] notifications;
    private String content;
    private Embed[] embeds;

    public String renderJson() {
        return new Gson().toJson(this);
    }

    public boolean isNotificationEnabled(CloudCheckType checkType) {
        return Arrays.stream(notifications).anyMatch(s -> s.equalsIgnoreCase(checkType.name()));
    }

    public static CloudDetection loadFromConfigSection(ConfigurationSection section) {
        CloudDetection cloudDetection = new CloudDetection();
        cloudDetection.setEnabled(section.getBoolean("enabled"));
        cloudDetection.setCooldownPerPlayerAndType(section.getInt("cooldown_per_player_and_type"));
        cloudDetection.setNotifications(section.getStringList("notifications").toArray(new String[0]));
        cloudDetection.setContent(section.getString("content"));
        cloudDetection.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        return cloudDetection;
    }

}

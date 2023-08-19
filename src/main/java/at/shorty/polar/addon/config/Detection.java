package at.shorty.polar.addon.config;

import com.google.gson.Gson;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import top.polar.api.user.event.type.CheckType;

import java.util.Arrays;

@Data
public class Detection {

    private String webhookUrl;
    private boolean enabled;
    private int cooldownPerPlayerAndType;
    private String[] notifications;
    private String content;
    private Embed[] embeds;

    public String renderJson() {
        return new Gson().toJson(this);
    }

    public boolean isNotificationEnabled(CheckType checkType) {
        return Arrays.stream(notifications).anyMatch(s -> s.equalsIgnoreCase(checkType.name()));
    }

    public static Detection loadFromConfigSection(ConfigurationSection section) {
        Detection detection = new Detection();
        detection.setWebhookUrl(section.getString("webhook_url"));
        detection.setEnabled(section.getBoolean("enabled"));
        detection.setCooldownPerPlayerAndType(section.getInt("cooldown_per_player_and_type"));
        detection.setNotifications(section.getStringList("notifications").toArray(new String[0]));
        detection.setContent(section.getString("content"));
        detection.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        return detection;
    }

}

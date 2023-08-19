package at.shorty.polar.addon.config;

import com.google.gson.Gson;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import top.polar.api.user.event.type.CheckType;

import java.util.Arrays;

@Data
public class Mitigation {

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

    public static Mitigation loadFromConfigSection(ConfigurationSection section) {
        Mitigation mitigation = new Mitigation();
        mitigation.setEnabled(section.getBoolean("enabled"));
        mitigation.setCooldownPerPlayerAndType(section.getInt("cooldown_per_player_and_type"));
        mitigation.setNotifications(section.getStringList("notifications").toArray(new String[0]));
        mitigation.setContent(section.getString("content"));
        mitigation.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        return mitigation;
    }

}

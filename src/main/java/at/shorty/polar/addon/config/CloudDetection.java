package at.shorty.polar.addon.config;

import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.Embed;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;
import top.polar.api.user.event.type.CloudCheckType;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Data
public class CloudDetection extends DiscordWebhook {

    private transient String webhookUrl;
    private transient boolean enabled;
    private transient int cooldownPerPlayerAndType;
    private transient String[] notifications;
    private transient String[] detailFilters;

    public boolean isNotificationEnabled(CloudCheckType checkType) {
        return Arrays.stream(notifications).anyMatch(s -> s.equalsIgnoreCase(checkType.name()));
    }

    public static CloudDetection loadFromConfigSection(ConfigurationSection section) {
        CloudDetection cloudDetection = new CloudDetection();
        cloudDetection.setWebhookUrl(section.getString("webhook_url"));
        cloudDetection.setEnabled(section.getBoolean("enabled"));
        cloudDetection.setCooldownPerPlayerAndType(section.getInt("cooldown_per_player_and_type"));
        cloudDetection.setNotifications(section.getStringList("notifications").toArray(new String[0]));
        cloudDetection.setDetailFilters(section.getStringList("filter_detail_lines").toArray(new String[0]));
        cloudDetection.setContent(section.getString("content"));
        cloudDetection.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        cloudDetection.initializeCache(cloudDetection.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
        return cloudDetection;
    }

}

package at.shorty.polar.addon.config;

import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.Embed;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;
import top.polar.api.user.event.type.CheckType;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Data
public class Detection extends DiscordWebhook {

    private transient String webhookUrl;
    private transient boolean enabled;
    private transient boolean roundVl;
    private transient int cooldownPerPlayerAndType;
    private transient String[] notifications;
    private transient String[] detailFilters;

    public boolean isNotificationEnabled(CheckType checkType) {
        return Arrays.stream(notifications).anyMatch(s -> s.equalsIgnoreCase(checkType.name()));
    }

    public static Detection loadFromConfigSection(ConfigurationSection section) {
        Detection detection = new Detection();
        detection.setWebhookUrl(section.getString("webhook_url"));
        detection.setEnabled(section.getBoolean("enabled"));
        detection.setRoundVl(section.getBoolean("round_vl"));
        detection.setCooldownPerPlayerAndType(section.getInt("cooldown_per_player_and_type"));
        detection.setNotifications(section.getStringList("notifications").toArray(new String[0]));
        detection.setDetailFilters(section.getStringList("filter_detail_lines").toArray(new String[0]));
        detection.setContent(section.getString("content"));
        detection.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        detection.initializeCache(detection.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
        return detection;
    }

}

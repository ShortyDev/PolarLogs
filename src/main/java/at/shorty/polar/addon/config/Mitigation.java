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
public class Mitigation extends DiscordWebhook {

    private transient String webhookUrl;
    private transient boolean enabled;
    private transient boolean roundVl;
    private transient int cooldownPerPlayerAndType;
    private transient String[] notifications;
    private transient String[] detailFilters;
    private transient double minVl;

    public boolean isNotificationEnabled(CheckType checkType) {
        return Arrays.stream(notifications).anyMatch(s -> s.equalsIgnoreCase(checkType.name()));
    }

    public static Mitigation loadFromConfigSection(ConfigurationSection section) {
        Mitigation mitigation = new Mitigation();
        mitigation.setWebhookUrl(section.getString("webhook_url"));
        mitigation.setEnabled(section.getBoolean("enabled"));
        mitigation.setRoundVl(section.getBoolean("round_vl"));
        mitigation.setCooldownPerPlayerAndType(section.getInt("cooldown_per_player_and_type"));
        mitigation.setNotifications(section.getStringList("notifications").toArray(new String[0]));
        mitigation.setDetailFilters(section.getStringList("filter_detail_lines").toArray(new String[0]));
        mitigation.setMinVl(section.getDouble("min_vl"));
        mitigation.setContent(section.getString("content"));
        mitigation.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        mitigation.initializeCache(mitigation.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
        return mitigation;
    }

}

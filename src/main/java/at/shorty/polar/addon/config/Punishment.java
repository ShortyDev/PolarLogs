package at.shorty.polar.addon.config;

import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.Embed;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = true)
@Data
public class Punishment extends DiscordWebhook {

    private transient String webhookUrl;
    private transient boolean enabled;
    private transient String[] typesEnabled;
    private transient int cooldownPerPlayer;

    public static Punishment loadFromConfigSection(ConfigurationSection section) {
        Punishment punishment = new Punishment();
        punishment.setWebhookUrl(section.getString("webhook_url"));
        punishment.setEnabled(section.getBoolean("enabled"));
        punishment.setTypesEnabled(section.getStringList("types_enabled").toArray(new String[0]));
        punishment.setCooldownPerPlayer(section.getInt("cooldown_per_player"));
        punishment.setContent(section.getString("content"));
        punishment.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        punishment.initializeCache(punishment.getCooldownPerPlayer(), TimeUnit.SECONDS);
        return punishment;
    }

}

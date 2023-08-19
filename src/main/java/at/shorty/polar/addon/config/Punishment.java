package at.shorty.polar.addon.config;

import com.google.gson.Gson;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;

@Data
public class Punishment {

    private boolean enabled;
    private String[] typesEnabled;
    private int cooldownPerPlayer;
    private String content;
    private Embed[] embeds;

    public String renderJson() {
        return new Gson().toJson(this);
    }

    public static Punishment loadFromConfigSection(ConfigurationSection section) {
        Punishment punishment = new Punishment();
        punishment.setEnabled(section.getBoolean("enabled"));
        punishment.setTypesEnabled(section.getStringList("types_enabled").toArray(new String[0]));
        punishment.setCooldownPerPlayer(section.getInt("cooldown_per_player"));
        punishment.setContent(section.getString("content"));
        punishment.setEmbeds(new Embed[]{Embed.loadFromConfigSection(section.getConfigurationSection("embed"))});
        return punishment;
    }

}

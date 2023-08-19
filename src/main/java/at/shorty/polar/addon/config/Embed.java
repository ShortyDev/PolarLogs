package at.shorty.polar.addon.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;

@Data
public class Embed {

    private String title;
    private String description;
    private int color;
    private Footer footer;
    private Thumbnail thumbnail;
    private Image image;
    private String timestamp;
    private Field[] fields;

    public static Embed loadFromConfigSection(ConfigurationSection configurationSection) {
        Embed embed = new Embed();
        embed.setTitle(configurationSection.getString("title"));
        embed.setDescription(configurationSection.getString("description"));
        embed.setColor((int) Long.parseLong(configurationSection.getString("color"), 16));
        Footer footer = new Footer(configurationSection.getString("footer.text"), configurationSection.getString("footer.icon_url"));
        Thumbnail thumbnail = new Thumbnail(configurationSection.getString("thumbnail.url"));
        Image image = new Image(configurationSection.getString("image.url"));
        embed.setTimestamp(configurationSection.getString("timestamp"));
        Field[] fields = new Field[configurationSection.getConfigurationSection("fields").getKeys(false).size()];
        int i = 0;
        for (String key : configurationSection.getConfigurationSection("fields").getKeys(false)) {
            fields[i] = new Field(configurationSection.getConfigurationSection("fields").getString(key + ".name"), configurationSection.getConfigurationSection("fields").getString(key + ".value"), configurationSection.getConfigurationSection("fields").getBoolean(key + ".inline"));
            i++;
        }
        embed.setFooter(footer);
        embed.setThumbnail(thumbnail);
        embed.setImage(image);
        embed.setFields(fields);
        return embed;
    }

    @AllArgsConstructor
    @Data
    public static class Footer {
        private String text;
        private String icon_url;
    }

    @AllArgsConstructor
    @Data
    public static class Thumbnail {
        private String url;
    }

    @AllArgsConstructor
    @Data
    public static class Image {
        private String url;
    }

    @AllArgsConstructor
    @Data
    public static class Field {
        private String name;
        private String value;
        private boolean inline;
    }

}

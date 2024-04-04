package at.shorty.polar.addon.hook;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.ratelimit.DefaultCooldown;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

@EqualsAndHashCode(callSuper = true)
@Data
public class DiscordWebhook extends DefaultCooldown {

    private String content;
    private Embed[] embeds;

    public static void sendWebhook(String webhookUrl, String content) {
        try {
            URL url = new URL(webhookUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);
            connection.getOutputStream().write(content.getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.getInputStream().close();
        } catch (IOException e) {
            PolarLogs.getPlugin(PolarLogs.class).getLogger().warning("Failed to send webhook message: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

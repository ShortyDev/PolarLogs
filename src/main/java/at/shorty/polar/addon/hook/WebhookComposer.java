package at.shorty.polar.addon.hook;

import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.config.Punishment;
import at.shorty.polar.addon.util.Json;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import top.polar.api.check.Check;
import top.polar.api.user.User;
import top.polar.api.user.event.CloudDetectionEvent;
import top.polar.api.user.event.DetectionAlertEvent;
import top.polar.api.user.event.MitigationEvent;
import top.polar.api.user.event.PunishmentEvent;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

@UtilityClass
public class WebhookComposer {

    public String composeMitigationWebhookMessage(Mitigation mitigation, MitigationEvent event) {
        String content = Json.render(mitigation);
        Check check = event.check();
        boolean roundVl = mitigation.isRoundVl();
        double vl = check.violationLevel();
        double punishVl = check.punishVl();
        int iVl = (int) vl;
        int iPunishVl = (int) punishVl;
        return content.replace("%VL%", roundVl ? String.valueOf(iVl) : String.valueOf(vl))
                .replace("%PUNISH_VL%", roundVl ? String.valueOf(iPunishVl) : String.valueOf(punishVl))
                .replace("%CHECK_TYPE%", check.type().name())
                .replace("%CHECK_NAME%", check.name())
                .replace("%DETAILS%", applyDetailFilters(event.details(), mitigation.getDetailFilters()));
    }

    public String composeDetectionWebhookMessage(Detection detection, DetectionAlertEvent event) {
        String content = Json.render(detection);
        Check check = event.check();
        boolean roundVl = detection.isRoundVl();
        double vl = check.violationLevel();
        double punishVl = check.punishVl();
        int iVl = (int) vl;
        int iPunishVl = (int) punishVl;
        return content.replace("%VL%", roundVl ? String.valueOf(iVl) : String.valueOf(vl))
                .replace("%PUNISH_VL%", roundVl ? String.valueOf(iPunishVl) : String.valueOf(punishVl))
                .replace("%CHECK_TYPE%", check.type().name())
                .replace("%CHECK_NAME%", check.name())
                .replace("%DETAILS%", applyDetailFilters(event.details(), detection.getDetailFilters()));
    }

    public String composeCloudDetectionWebhookMessage(CloudDetection cloudDetection, CloudDetectionEvent event) {
        String content = Json.render(cloudDetection);
        return content.replace("%CHECK_TYPE%", event.cloudCheckType().name())
                .replace("%DETAILS%", applyDetailFilters(event.details(), cloudDetection.getDetailFilters()));
    }

    public String composePunishmentWebhookMessage(Punishment punishment, PunishmentEvent event) {
        String content = Json.render(punishment);
        return content.replace("%PUNISHMENT%", event.type().name())
                .replace("%REASON%", event.reason());
    }

    public String replaceGlobalPlaceholders(String content, User user) {
        Player bukkitPlayer = user.bukkitPlayer().orElse(null);
        return content.replace("%PLAYER_NAME%", user.username())
                .replace("%PLAYER_UUID%", user.uuid().toString())
                .replace("%PLAYER_LATENCY%", String.valueOf(user.connection().latency()))
                .replace("%PLAYER_IP%", bukkitPlayer != null ? bukkitPlayer.getAddress().getAddress().getHostAddress() : "Unknown")
                .replace("%PLAYER_PROTOCOL_VERSION%", String.valueOf(user.clientVersion().protocolVersion()))
                .replace("%PLAYER_CLIENT_VERSION_NAME%", user.clientVersion().name())
                .replace("%PLAYER_CLIENT_BRAND%", user.clientVersion().brand())
                .replace("%TIMESTAMP%", Instant.now().atZone(ZoneOffset.UTC).toString())
                .replace("%TIMESTAMP_UNIX%", String.valueOf(Instant.now().getEpochSecond()));
    }

    private String applyDetailFilters(String details, String[] detailFilters) {
        if (detailFilters != null && detailFilters.length > 0) {
            String[] lines = details.split("\n");
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                if (line.isEmpty()) continue;
                line = ChatColor.translateAlternateColorCodes('&', line);
                line = ChatColor.stripColor(line);
                line = Pattern.compile("<(.*?)>").matcher(line).replaceAll("");
                line = line.trim();
                boolean filter = false;
                for (String filterLine : detailFilters) {
                    if (filterLine.startsWith("*") && filterLine.endsWith("*")) {
                        if (line.contains(filterLine.replace("*", ""))) {
                            filter = true;
                            break;
                        }
                    } else if (filterLine.startsWith("*")) {
                        if (line.endsWith(filterLine.replace("*", ""))) {
                            filter = true;
                            break;
                        }
                    } else if (filterLine.endsWith("*")) {
                        if (line.startsWith(filterLine.replace("*", ""))) {
                            filter = true;
                            break;
                        }
                    } else {
                        if (line.equals(filterLine)) {
                            filter = true;
                            break;
                        }
                    }
                }
                if (!filter) builder.append(line).append("\n");
            }
            details = builder.toString();
        }
        details = details.replace("\n", "\\n").trim();
        return details;
    }

}

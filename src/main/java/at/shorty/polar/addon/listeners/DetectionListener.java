package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import at.shorty.polar.addon.ratelimit.DefaultCooldown;
import org.bukkit.Bukkit;
import top.polar.api.user.event.DetectionAlertEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DetectionListener extends DefaultCooldown implements Consumer<DetectionAlertEvent> {

    private Detection detection;
    private Logs logs;

    public DetectionListener(Detection detection, Logs logs) {
        this.detection = detection;
        this.logs = logs;
        initializeCache(detection.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
    }

    public void reloadConfig(Detection detection, Logs logs) {
        this.detection = detection;
        this.logs = logs;
    }

    @Override
    public void accept(DetectionAlertEvent detectionAlertEvent) {
        if (detectionAlertEvent.cancelled()) return;
        if (detection.isEnabled() && detection.isNotificationEnabled(detectionAlertEvent.check().type())) {
            if (detection.getCooldownPerPlayerAndType() > 0 && detection.handleCooldown(detectionAlertEvent, Type.WEBHOOK)) return;
            String content = WebhookComposer.composeDetectionWebhookMessage(detection, detectionAlertEvent);
            content = WebhookComposer.replaceGlobalPlaceholders(content, detectionAlertEvent.user());
            DiscordWebhook.sendWebhook(detection.getWebhookUrl(), content);
        }
        if (logs.isEnabled() && logs.getStore().isDetection()) {
            if (detectionAlertEvent.details().equals("This is a test alert")) {
                return;
            }
            if (detection.getCooldownPerPlayerAndType() > 0 && detection.handleCooldown(detectionAlertEvent, Type.LOGS)) return;
            Bukkit.getServer().getScheduler().runTaskAsynchronously(PolarLogs.getPlugin(PolarLogs.class), () -> logs.logDetection(detectionAlertEvent.user(), detectionAlertEvent.check(), detectionAlertEvent.details()));
        }
    }
}

package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import at.shorty.polar.addon.ratelimit.DefaultCooldown;
import top.polar.api.user.event.DetectionAlertEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DetectionListener extends DefaultCooldown implements Consumer<DetectionAlertEvent> {

    private Detection detection;

    public DetectionListener(Detection detection) {
        this.detection = detection;
        initializeCache(detection.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
    }

    public void reloadConfig(Detection detection) {
        this.detection = detection;
    }

    @Override
    public void accept(DetectionAlertEvent detectionAlertEvent) {
        if (!detection.isEnabled()) return;
        if (!detection.isNotificationEnabled(detectionAlertEvent.check().type())) return;
        if (detection.getCooldownPerPlayerAndType() > 0 && detection.handleCooldown(detectionAlertEvent)) return;
        String content = WebhookComposer.composeDetectionWebhookMessage(detection, detectionAlertEvent);
        content = WebhookComposer.replaceGlobalPlaceholders(content, detectionAlertEvent.user());
        DiscordWebhook.sendWebhook(detection.getWebhookUrl(), content);
    }
}

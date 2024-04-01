package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import at.shorty.polar.addon.ratelimit.DefaultCooldown;
import org.bukkit.Bukkit;
import top.polar.api.user.event.CloudDetectionEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CloudDetectionListener extends DefaultCooldown implements Consumer<CloudDetectionEvent> {

    private CloudDetection cloudDetection;
    private Logs logs;

    public CloudDetectionListener(CloudDetection cloudDetection, Logs logs) {
        this.cloudDetection = cloudDetection;
        this.logs = logs;
        initializeCache(cloudDetection.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
    }

    public void reloadConfig(CloudDetection cloudDetection, Logs logs) {
        this.cloudDetection = cloudDetection;
        this.logs = logs;
    }

    @Override
    public void accept(CloudDetectionEvent cloudDetectionEvent) {
        if (!cloudDetection.isEnabled()) return;
        if (!cloudDetection.isNotificationEnabled(cloudDetectionEvent.cloudCheckType())) return;
        if (cloudDetection.getCooldownPerPlayerAndType() > 0 && cloudDetection.handleCooldown(cloudDetectionEvent)) return;
        String content = WebhookComposer.composeCloudDetectionWebhookMessage(cloudDetection, cloudDetectionEvent);
        content = WebhookComposer.replaceGlobalPlaceholders(content, cloudDetectionEvent.user());
        DiscordWebhook.sendWebhook(cloudDetection.getWebhookUrl(), content);
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PolarLogs.getPlugin(PolarLogs.class), () -> logs.logCloudDetection(cloudDetectionEvent.user(), cloudDetectionEvent.cloudCheckType(), cloudDetectionEvent.details()));
    }
}

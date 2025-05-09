package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import at.shorty.polar.addon.ratelimit.DefaultCooldown;
import org.bukkit.Bukkit;
import top.polar.api.user.event.MitigationEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MitigationListener extends DefaultCooldown implements Consumer<MitigationEvent> {

    private Mitigation mitigation;
    private Logs logs;

    public MitigationListener(Mitigation mitigation, Logs logs) {
        this.mitigation = mitigation;
        this.logs = logs;
        initializeCache(mitigation.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
    }

    public void reloadConfig(Mitigation mitigation, Logs logs) {
        this.mitigation = mitigation;
        this.logs = logs;
    }

    @Override
    public void accept(MitigationEvent mitigationEvent) {
        if (mitigationEvent.cancelled()) return;
        if (mitigation.isEnabled() && mitigation.isNotificationEnabled(mitigationEvent.check().type()) && mitigationEvent.check().violationLevel() >= mitigation.getMinVl()) {
            if (mitigation.getCooldownPerPlayerAndType() > 0 && handleCooldown(mitigationEvent, Type.WEBHOOK)) return;
            String content = WebhookComposer.composeMitigationWebhookMessage(mitigation, mitigationEvent);
            content = WebhookComposer.replaceGlobalPlaceholders(content, mitigationEvent.user());
            DiscordWebhook.sendWebhook(mitigation.getWebhookUrl(), content);
        }
        if (logs.isEnabled() && logs.getStore().isMitigation() && mitigationEvent.check().violationLevel() >= logs.getMitigationTuning().getMinVl()) {
            if (mitigation.getCooldownPerPlayerAndType() > 0 && handleCooldown(mitigationEvent, Type.LOGS)) return;
            Bukkit.getServer().getScheduler().runTaskAsynchronously(PolarLogs.getPlugin(PolarLogs.class), () -> logs.logMitigation(mitigationEvent.user(), mitigationEvent.check(), mitigationEvent.details()));
        }
    }
}

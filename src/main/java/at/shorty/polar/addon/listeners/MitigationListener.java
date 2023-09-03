package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import at.shorty.polar.addon.ratelimit.DefaultCooldown;
import top.polar.api.user.event.MitigationEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MitigationListener extends DefaultCooldown implements Consumer<MitigationEvent> {

    private final Mitigation mitigation;

    public MitigationListener(Mitigation mitigation) {
        this.mitigation = mitigation;
        initializeCache(mitigation.getCooldownPerPlayerAndType(), TimeUnit.SECONDS);
    }

    @Override
    public void accept(MitigationEvent mitigationEvent) {
        if (!mitigation.isEnabled()) return;
        if (!mitigation.isNotificationEnabled(mitigationEvent.check().type())) return;
        if (mitigationEvent.check().violationLevel() < mitigation.getMinVl()) return;
        if (mitigation.getCooldownPerPlayerAndType() > 0 && handleCooldown(mitigationEvent)) return;
        String content = WebhookComposer.composeMitigationWebhookMessage(mitigation, mitigationEvent);
        content = WebhookComposer.replaceGlobalPlaceholders(content, mitigationEvent.user());
        DiscordWebhook.sendWebhook(mitigation.getWebhookUrl(), content);
    }
}

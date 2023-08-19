package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.config.Punishment;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import lombok.AllArgsConstructor;
import top.polar.api.user.event.PunishmentEvent;

import java.util.function.Consumer;

@AllArgsConstructor
public class PunishmentListener implements Consumer<PunishmentEvent> {

    private Punishment punishment;

    @Override
    public void accept(PunishmentEvent punishmentEvent) {
        if (!punishment.isEnabled()) return;
        if (!String.join("", punishment.getTypesEnabled()).contains(punishmentEvent.type().name())) return;
        String content = WebhookComposer.composePunishmentWebhookMessage(punishment, punishmentEvent);
        content = WebhookComposer.replaceGlobalPlaceholders(content, punishmentEvent.user());
        DiscordWebhook.sendWebhook(punishment.getWebhookUrl(), content);
    }
}

package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.config.Punishment;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import top.polar.api.user.event.PunishmentEvent;

import java.util.function.Consumer;

@AllArgsConstructor
public class PunishmentListener implements Consumer<PunishmentEvent> {

    private Punishment punishment;
    private Logs logs;

    public void reloadConfig(Punishment punishment, Logs logs) {
        this.punishment = punishment;
        this.logs = logs;
    }

    @Override
    public void accept(PunishmentEvent punishmentEvent) {
        if (punishment.isEnabled() && String.join("", punishment.getTypesEnabled()).contains(punishmentEvent.type().name())) {
            String content = WebhookComposer.composePunishmentWebhookMessage(punishment, punishmentEvent);
            content = WebhookComposer.replaceGlobalPlaceholders(content, punishmentEvent.user());
            DiscordWebhook.sendWebhook(punishment.getWebhookUrl(), content);
        }
        if (logs.isEnabled() && logs.getStore().isPunishment()) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(PolarLogs.getPlugin(PolarLogs.class), () -> logs.logPunishment(punishmentEvent.user(), punishmentEvent.type(), punishmentEvent.reason()));
        }
    }
}

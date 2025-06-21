package at.shorty.polar.addon.listeners;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.config.Punishment;
import at.shorty.polar.addon.hook.DiscordWebhook;
import at.shorty.polar.addon.hook.WebhookComposer;
import at.shorty.polar.addon.ratelimit.DefaultCooldown;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import top.polar.api.user.event.PunishmentEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PunishmentListener extends DefaultCooldown implements Consumer<PunishmentEvent> {

    private Punishment punishment;
    private Logs logs;

    public PunishmentListener(Punishment punishment, Logs logs) {
        this.punishment = punishment;
        this.logs = logs;
        initializeCache(punishment.getCooldownPerPlayer(), TimeUnit.SECONDS);
    }

    public void reloadConfig(Punishment punishment, Logs logs) {
        this.punishment = punishment;
        this.logs = logs;
    }

    @Override
    public void accept(PunishmentEvent punishmentEvent) {
        if (punishmentEvent.cancelled()) return;
        if (punishment.isEnabled() && String.join("", punishment.getTypesEnabled()).contains(punishmentEvent.type().name())) {
            if (punishment.getCooldownPerPlayer() > 0 && handleCooldown(punishmentEvent, DefaultCooldown.Type.WEBHOOK)) return;
            String content = WebhookComposer.composePunishmentWebhookMessage(punishment, punishmentEvent);
            content = WebhookComposer.replaceGlobalPlaceholders(content, punishmentEvent.user());
            DiscordWebhook.sendWebhook(punishment.getWebhookUrl(), content);
        }
        if (logs.isEnabled() && logs.getStore().isPunishment()) {
            if (punishment.getCooldownPerPlayer() > 0 && handleCooldown(punishmentEvent, Type.LOGS)) return;
            Bukkit.getServer().getScheduler().runTaskAsynchronously(PolarLogs.getPlugin(PolarLogs.class), () ->
                    logs.logPunishment(punishmentEvent.user(), punishmentEvent.type(), punishmentEvent.reason()));
        }
    }
}

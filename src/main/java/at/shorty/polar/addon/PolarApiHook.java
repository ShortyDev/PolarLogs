package at.shorty.polar.addon;

import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.config.Punishment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.polar.api.PolarApi;
import top.polar.api.PolarApiAccessor;
import top.polar.api.exception.PolarNotLoadedException;
import top.polar.api.user.event.CloudDetectionEvent;
import top.polar.api.user.event.DetectionAlertEvent;
import top.polar.api.user.event.MitigationEvent;
import top.polar.api.user.event.PunishmentEvent;

import java.util.Map;

@AllArgsConstructor
@Getter
public class PolarApiHook implements Runnable {

    private String webhookUrl;
    private Mitigation mitigation;
    private Detection detection;
    private CloudDetection cloudDetection;
    private Punishment punishment;
    private Map<String, Long> cooldownCache;

    @Override
    public void run() {

        try {
            PolarApi polarApi = PolarApiAccessor.access().get();
            polarApi.events().repository().registerListener(MitigationEvent.class, mitigationEvent -> {
                if (!mitigation.isEnabled()) return;
                if (!mitigation.isNotificationEnabled(mitigationEvent.check().type())) return;
                if (mitigation.getCooldownPerPlayerAndType() > 0) {
                    String key = mitigationEvent.user().username() + mitigationEvent.check().type().name() + "-m";
                    if (cooldownCache.containsKey(key) && cooldownCache.get(key) > System.currentTimeMillis()) {
                        return;
                    }
                    cooldownCache.put(key, System.currentTimeMillis() + (mitigation.getCooldownPerPlayerAndType() * 1000L));
                }
                String content = Webhooks.replacePlaceholders(
                        mitigation.renderJson(),
                        mitigationEvent.user().username(),
                        String.valueOf(mitigationEvent.check().violationLevel()),
                        mitigationEvent.check().type().name(),
                        mitigationEvent.check().name(),
                        mitigationEvent.details(),
                        "",
                        "");
                Webhooks.sendWebhook(webhookUrl, content);
            });
            polarApi.events().repository().registerListener(DetectionAlertEvent.class, detectionAlertEvent -> {
                if (!detection.isEnabled()) return;
                if (!detection.isNotificationEnabled(detectionAlertEvent.check().type())) return;
                if (detection.getCooldownPerPlayerAndType() > 0) {
                    String key = detectionAlertEvent.user().username() + detectionAlertEvent.check().type().name() + "-d";
                    if (cooldownCache.containsKey(key) && cooldownCache.get(key) > System.currentTimeMillis()) {
                        return;
                    }
                    cooldownCache.put(key, System.currentTimeMillis() + (detection.getCooldownPerPlayerAndType() * 1000L));
                }
                String content = Webhooks.replacePlaceholders(
                        detection.renderJson(),
                        detectionAlertEvent.user().username(),
                        String.valueOf(detectionAlertEvent.check().violationLevel()),
                        detectionAlertEvent.check().type().name(),
                        detectionAlertEvent.check().name(),
                        detectionAlertEvent.details(),
                        "",
                        "");
                Webhooks.sendWebhook(webhookUrl, content);
            });
            polarApi.events().repository().registerListener(CloudDetectionEvent.class, cloudDetectionEvent -> {
                if (!cloudDetection.isEnabled()) return;
                if (!cloudDetection.isNotificationEnabled(cloudDetectionEvent.cloudCheckType())) return;
                if (cloudDetection.getCooldownPerPlayerAndType() > 0) {
                    String key = cloudDetectionEvent.user().username() + cloudDetectionEvent.cloudCheckType().name() + "-cd";
                    if (cooldownCache.containsKey(key) && cooldownCache.get(key) > System.currentTimeMillis()) {
                        return;
                    }
                    cooldownCache.put(key, System.currentTimeMillis() + (cloudDetection.getCooldownPerPlayerAndType() * 1000L));
                }
                String content = Webhooks.replacePlaceholders(
                        cloudDetection.renderJson(),
                        cloudDetectionEvent.user().username(),
                        "",
                        cloudDetectionEvent.cloudCheckType().name(),
                        "",
                        cloudDetectionEvent.details(),
                        "",
                        "");
                Webhooks.sendWebhook(webhookUrl, content);
            });
            polarApi.events().repository().registerListener(PunishmentEvent.class, punishmentEvent -> {
                if (!punishment.isEnabled()) return;
                if (!String.join("", punishment.getTypesEnabled()).contains(punishmentEvent.type().name())) return;
                String content = Webhooks.replacePlaceholders(
                        punishment.renderJson(),
                        punishmentEvent.user().username(),
                        "",
                        "",
                        "",
                        "",
                        punishmentEvent.type().name(),
                        punishmentEvent.reason());
                Webhooks.sendWebhook(webhookUrl, content);
            });
        } catch (PolarNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }
}

package at.shorty.polar.addon;

import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.config.Punishment;
import at.shorty.polar.addon.listeners.CloudDetectionListener;
import at.shorty.polar.addon.listeners.DetectionListener;
import at.shorty.polar.addon.listeners.MitigationListener;
import at.shorty.polar.addon.listeners.PunishmentListener;
import lombok.AllArgsConstructor;
import net.jodah.expiringmap.ExpiringMap;
import top.polar.api.PolarApi;
import top.polar.api.PolarApiAccessor;
import top.polar.api.exception.PolarNotLoadedException;
import top.polar.api.user.event.CloudDetectionEvent;
import top.polar.api.user.event.DetectionAlertEvent;
import top.polar.api.user.event.MitigationEvent;
import top.polar.api.user.event.PunishmentEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class PolarApiHook implements Runnable {

    private Mitigation mitigation;
    private Detection detection;
    private CloudDetection cloudDetection;
    private Punishment punishment;

    @Override
    public void run() {
        try {
            PolarApi polarApi = PolarApiAccessor.access().get();
            polarApi.events().repository().registerListener(MitigationEvent.class, new MitigationListener(mitigation));
            polarApi.events().repository().registerListener(DetectionAlertEvent.class, new DetectionListener(detection));
            polarApi.events().repository().registerListener(CloudDetectionEvent.class, new CloudDetectionListener(cloudDetection));
            polarApi.events().repository().registerListener(PunishmentEvent.class, new PunishmentListener(punishment));
        } catch (PolarNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V> Map<K, V> newExpiringMap(long expirationTime, TimeUnit timeUnit) {
        return ExpiringMap.builder().expiration(expirationTime, timeUnit).build();
    }

}

package at.shorty.polar.addon;

import at.shorty.polar.addon.config.CloudDetection;
import at.shorty.polar.addon.config.Detection;
import at.shorty.polar.addon.config.Mitigation;
import at.shorty.polar.addon.config.Punishment;
import at.shorty.polar.addon.listeners.CloudDetectionListener;
import at.shorty.polar.addon.listeners.DetectionListener;
import at.shorty.polar.addon.listeners.MitigationListener;
import at.shorty.polar.addon.listeners.PunishmentListener;
import lombok.RequiredArgsConstructor;
import top.polar.api.PolarApi;
import top.polar.api.PolarApiAccessor;
import top.polar.api.exception.PolarNotLoadedException;
import top.polar.api.user.event.CloudDetectionEvent;
import top.polar.api.user.event.DetectionAlertEvent;
import top.polar.api.user.event.MitigationEvent;
import top.polar.api.user.event.PunishmentEvent;

@RequiredArgsConstructor
public class PolarApiHook implements Runnable {

    private final Mitigation mitigation;
    private final Detection detection;
    private final CloudDetection cloudDetection;
    private final Punishment punishment;
    private MitigationListener mitigationListener;
    private DetectionListener detectionListener;
    private CloudDetectionListener cloudDetectionListener;
    private PunishmentListener punishmentListener;

    @Override
    public void run() {
        try {
            mitigationListener = new MitigationListener(mitigation);
            detectionListener = new DetectionListener(detection);
            cloudDetectionListener = new CloudDetectionListener(cloudDetection);
            punishmentListener = new PunishmentListener(punishment);
            PolarApi polarApi = PolarApiAccessor.access().get();
            polarApi.events().repository().registerListener(MitigationEvent.class, mitigationListener);
            polarApi.events().repository().registerListener(DetectionAlertEvent.class, detectionListener);
            polarApi.events().repository().registerListener(CloudDetectionEvent.class, cloudDetectionListener);
            polarApi.events().repository().registerListener(PunishmentEvent.class, punishmentListener);
        } catch (PolarNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadConfig(Mitigation mitigation, Detection detection, CloudDetection cloudDetection, Punishment punishment) {
        mitigationListener.reloadConfig(mitigation);
        detectionListener.reloadConfig(detection);
        cloudDetectionListener.reloadConfig(cloudDetection);
        punishmentListener.reloadConfig(punishment);
    }

}

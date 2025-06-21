package at.shorty.polar.addon;

import at.shorty.polar.addon.config.*;
import at.shorty.polar.addon.listeners.CloudDetectionListener;
import at.shorty.polar.addon.listeners.DetectionListener;
import at.shorty.polar.addon.listeners.MitigationListener;
import at.shorty.polar.addon.listeners.PunishmentListener;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import top.polar.api.PolarApi;
import top.polar.api.PolarApiAccessor;
import top.polar.api.check.Check;
import top.polar.api.event.listener.ListenerPriority;
import top.polar.api.exception.PolarNotLoadedException;
import top.polar.api.user.User;
import top.polar.api.user.config.ConfigOverride;
import top.polar.api.user.connection.ClientVersion;
import top.polar.api.user.connection.PlayerConnection;
import top.polar.api.user.event.*;
import top.polar.api.user.event.type.CheckType;
import top.polar.api.user.event.type.PunishmentType;
import top.polar.api.user.settings.AlertSettings;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PolarApiHook implements Runnable {

    private final Mitigation mitigation;
    private final Detection detection;
    private final CloudDetection cloudDetection;
    private final Punishment punishment;
    private final Logs logs;
    private MitigationListener mitigationListener;
    private DetectionListener detectionListener;
    private CloudDetectionListener cloudDetectionListener;
    private PunishmentListener punishmentListener;

    @Override
    public void run() {
        try {
            mitigationListener = new MitigationListener(mitigation, logs);
            detectionListener = new DetectionListener(detection, logs);
            cloudDetectionListener = new CloudDetectionListener(cloudDetection, logs);
            punishmentListener = new PunishmentListener(punishment, logs);
            PolarApi polarApi = PolarApiAccessor.access().get();
            polarApi.events().repository().registerListener(MitigationEvent.class, mitigationListener, ListenerPriority.RUN_LAST);
            polarApi.events().repository().registerListener(DetectionAlertEvent.class, detectionListener, ListenerPriority.RUN_LAST);
            polarApi.events().repository().registerListener(CloudDetectionEvent.class, cloudDetectionListener, ListenerPriority.RUN_LAST);
            polarApi.events().repository().registerListener(PunishmentEvent.class, punishmentListener, ListenerPriority.RUN_LAST);
        } catch (PolarNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadConfig(Mitigation mitigation, Detection detection, CloudDetection cloudDetection, Punishment punishment, Logs logs) {
        mitigationListener.reloadConfig(mitigation, logs);
        detectionListener.reloadConfig(detection, logs);
        cloudDetectionListener.reloadConfig(cloudDetection, logs);
        punishmentListener.reloadConfig(punishment, logs);
    }

    public void testWebhook() {
        detectionListener.accept(DetectionAlertEvent.builder().user(new User() {
                    @Override
                    public String username() {
                        return "Shorty";
                    }

                    @Override
                    public String profile() {
                        return "";
                    }

                    @Override
                    public int entityId() {
                        return 666;
                    }

                    @Override
                    public int ticksExisted() {
                        return 0;
                    }

                    @Override
                    public boolean bedrock() {
                        return false;
                    }

                    @Override
                    public UUID uuid() {
                        return UUID.randomUUID();
                    }

                    @Override
                    public ClientVersion clientVersion() {
                        return new ClientVersion() {
                            @Override
                            public int protocolVersion() {
                                return 12;
                            }

                            @Override
                            public String name() {
                                return "1.8.9";
                            }

                            @Override
                            public String brand() {
                                return "Polar";
                            }
                        };
                    }

                    @Override
                    public PlayerConnection connection() {
                        return new PlayerConnection() {
                            @Override
                            public long latency() {
                                return 10;
                            }

                            @Override
                            public double latencyAverage() {
                                return 10;
                            }

                            @Override
                            public double latencyDeviation() {
                                return 10;
                            }
                        };
                    }

                    @Override
                    public ConfigOverride configOverride() {
                        return null;
                    }

                    @Override
                    public Optional<Player> bukkitPlayer() {
                        return Optional.empty();
                    }

                    @Override
                    public void exempt() {
                    }

                    @Override
                    public boolean kick(String s) {
                        return false;
                    }

                    @Override
                    public boolean issueKickPunishment(String s) {
                        return false;
                    }

                    @Override
                    public boolean issueBanPunishment(String s) {
                        return false;
                    }

                    @Override
                    public AlertSettings detectionAlertSettings() {
                        return null;
                    }

                    @Override
                    public AlertSettings mitigationAlertSettings() {
                        return null;
                    }
                }).check(new Check() {
                    @Override
                    public CheckType type() {
                        return CheckType.REACH;
                    }

                    @Override
                    public String name() {
                        return "Reach";
                    }

                    @Override
                    public boolean punishable() {
                        return false;
                    }

                    @Override
                    public PunishmentType punishmentType() {
                        return null;
                    }

                    @Override
                    public boolean enabled() {
                        return true;
                    }

                    @Override
                    public double punishVl() {
                        return 0;
                    }

                    @Override
                    public double violationLevel() {
                        return 10;
                    }
                })
                .details("This is a test alert").build());
    }
}

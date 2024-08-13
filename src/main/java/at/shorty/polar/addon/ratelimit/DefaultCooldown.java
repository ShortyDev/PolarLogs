package at.shorty.polar.addon.ratelimit;

import lombok.Setter;
import net.jodah.expiringmap.ExpiringMap;
import top.polar.api.user.event.CloudDetectionEvent;
import top.polar.api.user.event.UserCancellableCheckEvent;
import top.polar.api.user.event.UserCancellableEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class DefaultCooldown {

    @Setter
    private transient Map<String, Object> webhooks;
    private transient Map<String, Object> logs;

    public void initializeCache(int time, TimeUnit timeUnit) {
        webhooks = ExpiringMap.builder()
                .expiration(time, timeUnit)
                .build();
        logs = ExpiringMap.builder()
                .expiration(time, timeUnit)
                .build();
    }

    public boolean handleCooldown(UserCancellableEvent event, Type type) {
        if (webhooks == null || logs == null) throw new IllegalStateException("Cache not initialized");
        if (isOnCooldown(event, type)) {
            return true;
        }
        applyCooldown(event, type);
        return false;
    }

    private void applyCooldown(UserCancellableEvent event, Type type) {
        if (webhooks == null || logs == null) throw new IllegalStateException("Cache not initialized");
        if (isOnCooldown(event, type)) return;
        String key = buildKey(event);
        if (type == Type.WEBHOOK) {
            webhooks.put(key, null);
        } else {
            logs.put(key, null);
        }
    }

    private boolean isOnCooldown(UserCancellableEvent event, Type type) {
        if (webhooks == null || logs == null) throw new IllegalStateException("Cache not initialized");
        String key = buildKey(event);
        return type == Type.WEBHOOK ? webhooks.containsKey(key) : logs.containsKey(key);
    }

    private String buildKey(UserCancellableEvent event) {
        String key = event.user().username();
        if (event instanceof UserCancellableCheckEvent) {
            key += ((UserCancellableCheckEvent) event).check().type().name();
        } else if (event instanceof CloudDetectionEvent) {
            key += ((CloudDetectionEvent) event).cloudCheckType().name();
        }
        return key;
    }

    public enum Type {
        LOGS, WEBHOOK
    }

}

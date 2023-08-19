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
    private Map<String, Object> cache;

    public void initializeCache(int time, TimeUnit timeUnit) {
        if (cache != null) throw new IllegalStateException("Cache already initialized");
        cache = ExpiringMap.builder()
                .expiration(time, timeUnit)
                .build();
    }

    public boolean handleCooldown(UserCancellableEvent event) {
        if (cache == null) throw new IllegalStateException("Cache not initialized");
        if (isOnCooldown(event)) {
            return true;
        }
        applyCooldown(event);
        return false;
    }

    private void applyCooldown(UserCancellableEvent event) {
        if (cache == null) throw new IllegalStateException("Cache not initialized");
        if (isOnCooldown(event)) return;
        String key = buildKey(event);
        cache.put(key, null);
    }

    private boolean isOnCooldown(UserCancellableEvent event) {
        if (cache == null) throw new IllegalStateException("Cache not initialized");
        String key = buildKey(event);
        return cache.containsKey(key);
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

}

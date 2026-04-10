package dev.sayaya.handbook.client.components;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

/**
 * 다른 사용자의 편집 위치(프레즌스)를 추적한다.
 * 30초 타임아웃으로 자동 해제.
 */
@Singleton
public class PresenceTracker {
    @Inject public PresenceTracker() {}
    private final Map<String, PresenceInfo> presenceMap = new LinkedHashMap<>();
    private final List<Consumer<Map<String, PresenceInfo>>> listeners = new ArrayList<>();

    public void update(String user, String userName, String type, String serial, String field) {
        if (type == null) {
            presenceMap.remove(user);
        } else {
            presenceMap.put(user, new PresenceInfo(user, userName, type, serial, field, System.currentTimeMillis()));
        }
        notifyListeners();
    }

    public void cleanExpired(long timeoutMs) {
        long now = System.currentTimeMillis();
        boolean removed = presenceMap.entrySet().removeIf(e -> now - e.getValue().timestamp > timeoutMs);
        if (removed) notifyListeners();
    }

    public Map<String, PresenceInfo> getAll() {
        return Collections.unmodifiableMap(presenceMap);
    }

    public void subscribe(Consumer<Map<String, PresenceInfo>> listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        var snapshot = Collections.unmodifiableMap(presenceMap);
        listeners.forEach(l -> l.accept(snapshot));
    }

    public static class PresenceInfo {
        public final String user;
        public final String userName;
        public final String type;
        public final String serial;
        public final String field;
        public final long timestamp;

        public PresenceInfo(String user, String userName, String type, String serial, String field, long timestamp) {
            this.user = user;
            this.userName = userName;
            this.type = type;
            this.serial = serial;
            this.field = field;
            this.timestamp = timestamp;
        }
    }
}

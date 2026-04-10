package dev.sayaya.handbook.client.components;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

/**
 * 다른 사용자의 편집 위치(프레즌스)를 추적하는 싱글턴 상태 저장소.
 *
 * <p><b>책임:</b> 사용자별 편집 위치(타입, 문서 serial, 필드)를 Map으로 관리.
 * type이 null이면 해당 사용자의 프레즌스를 해제한다.
 * 리스너 패턴으로 UI 렌더러에 변경을 통지한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 상태 저장소). document-ui, type-ui의 EventHandler가 update()를 호출하고,
 * SpreadsheetElement/CanvasElement가 subscribe()로 렌더링한다.</p>
 *
 * <p><b>주의:</b> GWT 싱글 스레드이므로 동기화 불필요. {@link #cleanExpired(long)}은
 * Timer 등으로 주기적 호출이 필요하다 (30초 권장).</p>
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

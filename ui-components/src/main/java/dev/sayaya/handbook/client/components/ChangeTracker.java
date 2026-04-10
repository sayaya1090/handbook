package dev.sayaya.handbook.client.components;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 키 기반 변경 상태(더티) 추적기. 도메인 객체에서 분리하여 순수성을 유지한다.
 *
 * <p><b>책임:</b> 각 키(document serial 또는 typeId:version)의 변경 상태를
 * NOT_CHANGED / CHANGED / DELETED로 추적. Save 시 {@link #getChangedKeys()}와
 * {@link #getDeletedKeys()}로 PATCH/DELETE 대상을 결정한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 상태 저장소). Action 구현체들이 markChanged/markDeleted를 호출하고,
 * SaveAction이 상태를 조회한다.</p>
 *
 * <p><b>주의:</b> {@link #unmark(String)}은 Undo 시 원래 상태로 복원할 때 사용.
 * {@link #reset()}은 Save 성공 후 전체 초기화.</p>
 */
@Singleton
public class ChangeTracker {
    public enum ChangeState { NOT_CHANGED, CHANGED, DELETED }

    private final Map<String, ChangeState> states = new HashMap<>();

    @Inject public ChangeTracker() {}

    public ChangeState getState(String key) {
        return states.getOrDefault(key, ChangeState.NOT_CHANGED);
    }

    public void markChanged(String key) {
        states.put(key, ChangeState.CHANGED);
    }

    public void markDeleted(String key) {
        states.put(key, ChangeState.DELETED);
    }

    public void unmark(String key) {
        states.remove(key);
    }

    public void reset() {
        states.clear();
    }

    public boolean hasChanges() {
        return !states.isEmpty();
    }

    public Set<String> getChangedKeys() {
        return states.entrySet().stream()
                .filter(e -> e.getValue() == ChangeState.CHANGED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<String> getDeletedKeys() {
        return states.entrySet().stream()
                .filter(e -> e.getValue() == ChangeState.DELETED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}

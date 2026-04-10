package dev.sayaya.handbook.client.usecase;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 타입별 변경 상태 추적. 도메인(TypeValue)에서 분리하여 순수성을 유지한다.
 * key = "typeId:typeVersion"
 */
@Singleton
public class ChangeTracker {
    public enum ChangeState { NOT_CHANGED, CHANGED, DELETED }

    private final Map<String, ChangeState> states = new HashMap<>();

    @Inject ChangeTracker() {}

    public ChangeState getState(String typeKey) {
        return states.getOrDefault(typeKey, ChangeState.NOT_CHANGED);
    }

    public void markChanged(String typeKey) {
        states.put(typeKey, ChangeState.CHANGED);
    }

    public void markDeleted(String typeKey) {
        states.put(typeKey, ChangeState.DELETED);
    }

    public void unmark(String typeKey) {
        states.remove(typeKey);
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

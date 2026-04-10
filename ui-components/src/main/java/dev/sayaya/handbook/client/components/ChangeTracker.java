package dev.sayaya.handbook.client.components;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 키 기반 변경 상태 추적. 도메인 객체에서 분리하여 순수성을 유지한다.
 * document-ui에서는 serial, type-ui에서는 "typeId:typeVersion"을 키로 사용한다.
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

package dev.sayaya.handbook.client.components;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Objects;
import java.util.function.BiPredicate;

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
    private final Map<String, Object> payloads = new HashMap<>();
    private final Map<String, Object> originalValues = new HashMap<>();
    private final dev.sayaya.rx.subject.BehaviorSubject<Boolean> hasChanges = dev.sayaya.rx.subject.BehaviorSubject.behavior(false);

    @Inject public ChangeTracker() {}

    public ChangeState getState(String key) {
        return states.getOrDefault(key, ChangeState.NOT_CHANGED);
    }

    public void markChanged(String key) {
        states.put(key, ChangeState.CHANGED);
        hasChanges.next(true);
    }

    /**
     * 값 비교를 통해 정밀하게 변경 여부를 추적한다.
     * 최초 변경 시 원본(original) 값을 저장해두고, 이후 현재 값(current)이 원본과 동일해지면
     * 'changed' 상태를 자동으로 해제(unmark)한다.
     */
    public <T> void trackChange(String key, T original, T current, BiPredicate<T, T> equalsFn) {
        // 최초 상태 기록
        if (!originalValues.containsKey(key) && !states.containsKey(key)) {
            originalValues.put(key, original);
        }
        
        @SuppressWarnings("unchecked")
        T savedOriginal = (T) originalValues.get(key);
        
        if (equalsFn.test(savedOriginal, current)) {
            unmark(key);
            originalValues.remove(key);
        } else {
            markChanged(key);
        }
    }
    
    public <T> void trackChange(String key, T original, T current) {
        trackChange(key, original, current, Objects::equals);
    }

    public void markDeleted(String key) {
        states.put(key, ChangeState.DELETED);
        hasChanges.next(true);
    }

    public void markDeleted(String key, Object payload) {
        states.put(key, ChangeState.DELETED);
        payloads.put(key, payload);
        hasChanges.next(true);
    }

    /**
     * 기존 키(oldKey)의 변경 상태와 원본 값을 새 키(newKey)로 복사(상속)한다.
     * 스키마 진화 등에서 새로운 버전을 생성할 때 기존 변경 이력을 유지하기 위해 사용된다.
     */
    public void inherit(String oldKey, String newKey) {
        if (states.containsKey(oldKey)) {
            states.put(newKey, states.get(oldKey));
        }
        if (originalValues.containsKey(oldKey)) {
            originalValues.put(newKey, originalValues.get(oldKey));
        }
        if (payloads.containsKey(oldKey)) {
            payloads.put(newKey, payloads.get(oldKey));
        }
    }

    public void unmark(String key) {
        states.remove(key);
        payloads.remove(key);
        hasChanges.next(!states.isEmpty());
    }

    public void reset() {
        states.clear();
        payloads.clear();
        originalValues.clear();
        hasChanges.next(false);
    }

    public boolean hasChanges() {
        return hasChanges.getValue();
    }
    
    public dev.sayaya.rx.Observable<Boolean> hasChangesObservable() {
        return hasChanges.asObservable();
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

    @SuppressWarnings("unchecked")
    public <T> T getDeletedPayload(String key) {
        return (T) payloads.get(key);
    }
}

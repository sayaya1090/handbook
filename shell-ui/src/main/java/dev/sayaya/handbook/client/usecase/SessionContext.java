package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 쉘의 확장 가능한 세션 컨텍스트를 관리하는 싱글톤.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>workspaceId 등 동적 컨텍스트 변수들을 키-값 맵으로 관리</li>
 *   <li>변수 변경 시 구독자에게 알릴 수 있도록 {@link BehaviorSubject} 노출</li>
 *   <li>{@link PlaceholderResolver} 가 예약어({@code {workspaceId}})를 치환할 때의 원천 데이터 제공</li>
 * </ul></p>
 */
@Singleton
public class SessionContext {
    private final Map<String, String> values = new HashMap<>();
    private final BehaviorSubject<Map<String, String>> subject = behavior(new HashMap<>());

    @Inject
    SessionContext() {}

    /**
     * 컨텍스트 변수를 설정하거나 제거한다.
     * @param key 변수명 (예: "workspaceId")
     * @param value 값. null 이면 해당 키 제거.
     */
    public void set(String key, String value) {
        if (value == null) values.remove(key);
        else values.put(key, value);
        subject.next(new HashMap<>(values));
    }

    /**
     * 특정 컨텍스트 변수 값을 조회한다.
     */
    public String get(String key) {
        return values.get(key);
    }

    /**
     * 현재 모든 컨텍스트 변수의 복사본을 반환한다.
     */
    public Map<String, String> getAll() {
        return new HashMap<>(values);
    }

    /**
     * 컨텍스트 변경을 구독한다.
     */
    public void subscribe(java.util.function.Consumer<Map<String, String>> consumer) {
        subject.subscribe(consumer::accept);
    }
}

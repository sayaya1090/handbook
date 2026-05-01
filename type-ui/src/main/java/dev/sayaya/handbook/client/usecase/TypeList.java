package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 로딩된 전체 타입 목록의 반응형 상태 컨테이너.
 *
 * <p><b>책임:</b> {@link BehaviorSubject}를 통해 현재 타입 목록을 관리하며,
 * 추가(add), 제거(remove), 갱신(update), 전체 교체(replace) 연산과
 * 구독(subscribe) 기능을 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link BehaviorSubject} — 반응형 상태 관리</li>
 *   <li>{@link TypeValue} — 보유하는 타입 도메인 객체</li>
 * </ul></p>
 * <p><b>주의:</b> 모든 변경은 새 Set 인스턴스를 생성하여 발행하므로 구독자에게 참조 동등성이 보장되지 않는다.</p>
 */
@Singleton
public class TypeList {
    private final BehaviorSubject<Set<TypeValue>> subject = behavior(Collections.emptySet());

    @Inject TypeList() {}

    public Observable<Set<TypeValue>> observable() {
        return subject.asObservable();
    }

    public Set<TypeValue> getValue() {
        return subject.getValue();
    }

    public void replace(Set<TypeValue> types) {
        subject.next(types);
    }

    public void add(TypeValue type) {
        Set<TypeValue> next = new LinkedHashSet<>(subject.getValue());
        next.add(type);
        subject.next(next);
    }

    public void remove(TypeValue type) {
        Set<TypeValue> next = new LinkedHashSet<>(subject.getValue());
        next.removeIf(t -> t.key().equals(type.key()));
        subject.next(next);
    }

    public void update(TypeValue before, TypeValue after) {
        Set<TypeValue> next = new LinkedHashSet<>();
        for (TypeValue t : subject.getValue()) {
            if (t.key().equals(before.key())) next.add(after);
            else next.add(t);
        }
        subject.next(next);
    }

    public void subscribe(Consumer<Set<TypeValue>> consumer) {
        subject.subscribe(consumer::accept);
    }
}

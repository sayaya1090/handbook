package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 
 * 전체 레이아웃 목록.
 * 
 * <p><b>책임:</b> 서버에서 조회된 {@link TypeLayout} 목록을 보유하며, 
 * 레이아웃 ID(UUID)를 보존하여 저장 시 정확한 업데이트가 가능하도록 한다.</p>
 */
@Singleton
public class LayoutList {
    private final BehaviorSubject<List<TypeLayout>> subject = behavior(Collections.emptyList());

    @Inject LayoutList() {}

    public Observable<List<TypeLayout>> observable() {
        return subject.asObservable();
    }

    public List<TypeLayout> getValue() {
        return subject.getValue();
    }

    public void replace(List<TypeLayout> layouts) {
        subject.next(layouts);
    }

    public void update(TypeLayout before, TypeLayout after) {
        java.util.List<TypeLayout> next = new java.util.ArrayList<>();
        for (TypeLayout l : subject.getValue()) {
            if (l.id() != null && l.id().equals(before.id())) next.add(after);
            else if (l == before) next.add(after); // Fallback for identity
            else next.add(l);
        }
        subject.next(next);
    }

    public void subscribe(Consumer<List<TypeLayout>> consumer) {
        subject.subscribe(consumer::accept);
    }
}

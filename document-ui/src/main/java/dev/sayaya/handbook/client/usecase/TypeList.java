package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class TypeList {
    @Delegate private final BehaviorSubject<Map<String, Map<String, Type>>> subject = behavior(Map.of());
    @Inject TypeList(Observable<Workspace> workspace) {
        /*workspace.switchMap(currentWorkspace -> {
            if (currentWorkspace == null || currentWorkspace.id() == null) return Observable.of(List.<Type>of());
            Observable<List<Period>> initialPeriods = layoutRepository.layouts().take(1);
            Observable<List<Period>> calculatedPeriods = calculatedLayoutProvider.asObservable().filter(s->s!=null && !s.isEmpty());
            return initialPeriods.concatWith(calculatedPeriods).distinctUntilChanged();  // 초기 로드 후 타입 변경에 따른 업데이트를 순차적으로 발행
        }).subscribe(this::next);*/
        this.subscribe(types-> DomGlobal.console.log(types));
        workspace.subscribe(w-> {
            if (w == null || w.id() == null) return;
            DomGlobal.console.log("workspace changed");
        });
    }
}
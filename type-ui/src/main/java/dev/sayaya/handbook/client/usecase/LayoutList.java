package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// workspaceObservable이 새로운 Workspace 객체를 발행할 때마다 (워크스페이스 변경 시)
// switchMap을 사용하여 이전의 layoutRepository.layouts() 구독을 취소하고
// 새로운 Workspace 컨텍스트에 맞는 Period 목록을 layoutRepository.layouts()를 통해 다시 가져옵니다.
// layoutRepository.layouts() 메소드는 현재 활성화된 워크스페이스에 대한 Period 목록을 반환하도록 내부적으로 구현되어 있다고 가정합니다.
@Singleton
public class LayoutList {
    @Delegate private final BehaviorSubject<List<Period>> subject = behavior(List.of());
    @Inject LayoutList(Observable<Workspace> workspace, LayoutRepository layoutRepository, CalculatedLayoutProvider calculatedLayoutProvider) {
        workspace.switchMap(currentWorkspace -> {
            if (currentWorkspace == null || currentWorkspace.id() == null) return Observable.of(List.<Period>of());
            Observable<List<Period>> initialPeriods = layoutRepository.layouts().take(1);
            Observable<List<Period>> calculatedPeriods = calculatedLayoutProvider.asObservable().filter(s->s!=null && !s.isEmpty());
            return initialPeriods.concatWith(calculatedPeriods).distinctUntilChanged();  // 초기 로드 후 타입 변경에 따른 업데이트를 순차적으로 발행
        }).subscribe(this::next);
    }

    public void next(List<Period> periods) {
        List<Period> list;
        if(periods!=null) list = periods.stream().sorted(Comparator.comparing(Period::effectDateTime)).collect(Collectors.toUnmodifiableList());
        else list = Collections.emptyList();
        this.subject.next(list);
    }
    public int findIndex(Period date) {
        if (date == null || getValue().isEmpty()) return -1;
        return getValue().indexOf(date);
    }
    public Period get(int index) {
        return getValue().get(index);
    }
    public int size() {
        return getValue().size();
    }
}
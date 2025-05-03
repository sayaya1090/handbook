package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class LayoutList {
    @Delegate private final BehaviorSubject<List<Period>> subject = behavior(List.of());
    @Inject LayoutList(Observable<Workspace> workspace, LayoutRepository repo) {
        workspace.distinctUntilChanged().subscribe(ws->repo.layouts().subscribe(this::next));
    }
    public void next(Period[] periods) {
        List<Period> list;
        if(periods!=null) list = Arrays.stream(periods).sorted(Comparator.comparing(Period::effectDateTime)).collect(Collectors.toUnmodifiableList());
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
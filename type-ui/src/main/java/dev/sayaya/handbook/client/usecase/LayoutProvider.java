package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class LayoutProvider {
    @Delegate private final BehaviorSubject<Period> subject = behavior(null);
    @Inject LayoutProvider(LayoutList layoutList) {
        layoutList.subscribe(this::setToLatest);
    }
    private void setToLatest(List<Period> periods) {
        subject.next(periods.isEmpty() ? null : periods.get(periods.size()-1));
    }
}

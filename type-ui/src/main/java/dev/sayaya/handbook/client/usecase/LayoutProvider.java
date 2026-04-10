package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.LayoutPeriod;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** 현재 선택된 레이아웃 기간. 기간이 바뀌면 타입 목록을 다시 로딩해야 한다. */
@Singleton
public class LayoutProvider {
    private final BehaviorSubject<LayoutPeriod> subject = behavior(null);

    @Inject LayoutProvider() {}

    public Observable<LayoutPeriod> observable() {
        return subject.asObservable();
    }

    public LayoutPeriod getValue() {
        return subject.getValue();
    }

    public void next(LayoutPeriod period) {
        subject.next(period);
    }

    public void subscribe(Consumer<LayoutPeriod> consumer) {
        subject.subscribe(consumer::accept);
    }

    /** 새 기간 목록이 들어오면 현재 선택과 가장 많이 겹치는 기간을 자동 선택한다. */
    public void selectBestMatch(List<LayoutPeriod> periods) {
        if (periods == null || periods.isEmpty()) return;
        LayoutPeriod current = subject.getValue();
        if (current == null) {
            subject.next(periods.get(0));
            return;
        }
        LayoutPeriod best = periods.get(0);
        double bestOverlap = 0;
        for (LayoutPeriod p : periods) {
            double overlap = current.overlap(p);
            if (overlap > bestOverlap) {
                bestOverlap = overlap;
                best = p;
            }
        }
        subject.next(best);
    }
}

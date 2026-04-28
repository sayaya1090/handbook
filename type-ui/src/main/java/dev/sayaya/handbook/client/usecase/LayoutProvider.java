package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 선택된 레이아웃 기간의 반응형 상태 컨테이너.
 *
 * <p><b>책임:</b> {@link BehaviorSubject}를 통해 현재 선택된 {@link LayoutPeriod}를 관리하며,
 * 기간 변경 시 구독자에게 알린다. 새 기간 목록이 들어오면 현재 선택과 가장 많이
 * 겹치는 기간을 자동 선택(selectBestMatch)하는 편의 메서드를 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link BehaviorSubject} — 반응형 상태 관리</li>
 *   <li>{@link LayoutPeriod} — 관리 대상 기간 값 객체</li>
 * </ul></p>
 * <p><b>주의:</b> 기간이 바뀌면 타입 목록을 다시 로딩해야 한다. 이 책임은 구독자(LoadAction 등)에 있다.</p>
 */
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

    public void replace(LayoutPeriod period) {
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

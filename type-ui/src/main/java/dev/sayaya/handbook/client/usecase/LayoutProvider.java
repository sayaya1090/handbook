package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 선택된 레이아웃의 반응형 상태 컨테이너.
 * 
 * <p><b>책임:</b> 현재 활성화된 {@link TypeLayout}을 관리하며, 
 * 레이아웃 전환 시 이를 구독자(CanvasElement, LoadAction 등)에게 통지한다.</p>
 */
@Singleton
public class LayoutProvider {
    private final BehaviorSubject<TypeLayout> subject = behavior(null);

    @Inject 
    LayoutProvider() {}

    public Observable<TypeLayout> observable() {
        return subject.asObservable();
    }

    public TypeLayout getValue() {
        return subject.getValue();
    }

    public void replace(TypeLayout layout) {
        subject.next(layout);
    }

    public void subscribe(Consumer<TypeLayout> consumer) {
        subject.subscribe(consumer::accept);
    }

    /** 
     * 새 레이아웃 목록이 들어오면 현재 선택과 가장 많이 겹치는 레이아웃을 자동 선택한다.
     */
    public void selectBestMatch(List<TypeLayout> layouts) {
        if (layouts == null || layouts.isEmpty()) return;
        TypeLayout current = subject.getValue();
        if (current == null) {
            // 초기 진입 시 가장 최신 레이아웃을 선택
            TypeLayout latest = layouts.get(0);
            for (TypeLayout p : layouts) {
                if (p.effectDateTime() > latest.effectDateTime()) latest = p;
            }
            subject.next(latest);
            return;
        }
        
        TypeLayout best = layouts.get(0);
        double maxOverlap = -1;

        elemental2.dom.DomGlobal.console.log("[LayoutProvider] Selecting best match for " + layouts.size() + " candidates.");

        for (TypeLayout p : layouts) {
            double overlap = current.toPeriod().overlap(p.toPeriod());
            elemental2.dom.DomGlobal.console.log("[LayoutProvider] Candidate: " + p.effectDateTime() + " ~ " + p.expireDateTime() + " | Overlap: " + overlap);

            boolean isBetter = overlap > maxOverlap + 1.0; // 1ms 이상 더 많이 겹치면 선택
            if (!isBetter && Math.abs(overlap - maxOverlap) <= 1.0 && maxOverlap >= 0) {
                // 타이브레이커: 겹침이 같으면 더 미래의 구간을 선택
                if (Math.abs(p.effectDateTime() - current.effectDateTime()) < 1.0) isBetter = true;
                else if (p.effectDateTime() > best.effectDateTime()) isBetter = true;
            }
            
            if (isBetter || maxOverlap < 0) {
                maxOverlap = overlap;
                best = p;
            }
        }
        elemental2.dom.DomGlobal.console.log("[LayoutProvider] Best match selected: " + best.effectDateTime() + " ~ " + best.expireDateTime());
        subject.next(best);
    }
}

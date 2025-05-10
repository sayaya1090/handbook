package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class LayoutProvider {
    @Delegate private final BehaviorSubject<Period> subject = behavior(null);
    @Inject LayoutProvider(LayoutList layoutList) {
        layoutList.subscribe(this::updateSelectedPeriod);
    }
    private void updateSelectedPeriod(List<Period> newPeriods) {
        if (newPeriods == null || newPeriods.isEmpty()) {
            subject.next(null);
            return;
        }
        final Period currentSelectedPeriod = subject.getValue();
        // 새로운 Period 목록에서 마지막 Period를 기본 선택(fallback)으로 사용합니다.
        // 이는 현재 선택된 Period가 없거나 겹치는 Period를 찾지 못한 경우에 사용됩니다.
        final Period fallbackPeriod = newPeriods.get(newPeriods.size() - 1);
        if (currentSelectedPeriod == null) {
            // 현재 선택된 Period가 없으면, fallback Period로 설정합니다.
            subject.next(fallbackPeriod);
            return;
        }
        // newPeriods 목록에서 currentSelectedPeriod와 가장 많이 겹치는 Period를 찾습니다.
        var bestMatchOpt = newPeriods.stream()
                .map(newPeriod -> new Object() { // 겹침 계산을 위해 Period와 겹침 기간을 함께 저장하는 익명 객체
                    final Period period = newPeriod;
                    final long overlap = calculateOverlapDuration(currentSelectedPeriod, newPeriod);
                }).filter(item -> item.overlap > 0) // 실제로 겹치는 Period만 고려합니다.
                .max(Comparator.comparingLong(item -> item.overlap)) // 겹치는 기간이 가장 긴 것을 찾습니다.
                .map(item -> item.period); // 해당 Period 객체를 추출합니다.

        // 가장 많이 겹치는 Period가 있으면 그것으로 설정하고, 없으면 fallback Period로 설정합니다.
        subject.next(bestMatchOpt.orElse(fallbackPeriod));
    }
    /**
     * 두 Period 객체 간의 겹치는 시간(밀리초)을 계산합니다.
     * @param p1 첫 번째 Period
     * @param p2 두 번째 Period
     * @return 겹치는 시간(밀리초). 겹치지 않으면 0을 반환합니다.
     */
    private long calculateOverlapDuration(Period p1, Period p2) {
        // p1 (currentSelectedPeriod)은 null일 수 있으므로 호출 전에 확인합니다.
        // p2 (newPeriod from list)는 null이 아니라고 가정합니다.
        long start1 = p1.effectDateTime().getTime();
        long end1 = p1.expireDateTime().getTime();
        long start2 = p2.effectDateTime().getTime();
        long end2 = p2.expireDateTime().getTime();

        long effectiveStart = Math.max(start1, start2);
        long effectiveEnd = Math.min(end1, end2);

        if (effectiveStart < effectiveEnd) return effectiveEnd - effectiveStart;
        return 0L;
    }
}
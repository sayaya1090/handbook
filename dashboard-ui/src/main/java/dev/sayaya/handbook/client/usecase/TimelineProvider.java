package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.TimelineData;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 타임라인 통계 데이터의 반응형 상태 홀더.
 *
 * <p><b>책임:</b> BehaviorSubject로 TimelineData 배열을 관리하고,
 * UI 컴포넌트가 구독할 수 있는 Observable을 제공한다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link BehaviorSubject} — 반응형 상태 관리</li></ul></p>
 */
@Singleton
public class TimelineProvider {
    @Delegate private final BehaviorSubject<TimelineData[]> _this = behavior(new TimelineData[0]);
    @Inject TimelineProvider() {}
}

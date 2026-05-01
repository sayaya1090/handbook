package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import elemental2.dom.MediaQueryList;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 브라우저 뷰포트 크기 변경을 감지하여 모바일/컴팩트 상태를 발행하는 옵저버.
 *
 * <p><b>책임:</b> CSS Media Query 를 감시하여 화면 레이아웃 결정 기준(Mobile/Compact)을
 * BehaviorSubject 로 실시간 공유한다.</p>
 *
 * <p><b>주의:</b> <ul>
 *   <li>Mobile: 768px 미만</li>
 *   <li>Compact: 480px 미만 (초소형 기기 또는 좁은 사이드바)</li>
 * </ul></p>
 */
public class ViewportObserver {
    private static final String MOBILE_QUERY = "(max-width: 768px)";
    private static final String COMPACT_QUERY = "(max-width: 480px)";

    private final BehaviorSubject<Boolean> mobile;
    private final BehaviorSubject<Boolean> compact;

    public ViewportObserver() {
        MediaQueryList mobileMedia = DomGlobal.window.matchMedia(MOBILE_QUERY);
        MediaQueryList compactMedia = DomGlobal.window.matchMedia(COMPACT_QUERY);

        this.mobile = behavior(mobileMedia.matches);
        this.compact = behavior(compactMedia.matches);

        mobileMedia.addListener(e -> mobile.next(mobileMedia.matches));
        compactMedia.addListener(e -> compact.next(compactMedia.matches));
    }

    /** 뷰포트 < 768px 여부를 관찰한다. */
    public Observable<Boolean> isMobile() { return mobile.asObservable(); }

    /** 뷰포트 < 480px 여부를 관찰한다. */
    public Observable<Boolean> isCompact() { return compact.asObservable(); }

    /** 현재 모바일 상태를 반환한다. */
    public boolean isMobileNow() { return mobile.getValue(); }

    /** 현재 컴팩트 상태를 반환한다. */
    public boolean isCompactNow() { return compact.getValue(); }
}

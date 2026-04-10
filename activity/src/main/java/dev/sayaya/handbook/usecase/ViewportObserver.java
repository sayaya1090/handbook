package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import elemental2.dom.MediaQueryList;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 뷰포트 크기 변경을 감지하여 모바일/컴팩트 상태를 BehaviorSubject로 발행한다.
 * <ul>
 *   <li>mobile: 뷰포트 너비 < 768px</li>
 *   <li>compact: 뷰포트 너비 < 480px</li>
 * </ul>
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

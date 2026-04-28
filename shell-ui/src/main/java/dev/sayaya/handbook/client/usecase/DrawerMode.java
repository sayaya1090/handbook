package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.DrawerState;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 드로어 상태를 관리한다.
 * 메뉴나 도구가 변경되면 자동으로 접힌다.
 * 모바일(뷰포트 < 768px)에서는 OVERLAY 모드로 전환된다.
 */
@Singleton
public class DrawerMode {
    @Delegate private final BehaviorSubject<DrawerState> _this = behavior(DrawerState.EXPAND);
    private boolean mobile;

    @Inject DrawerMode(MenuSelected menuSelected, ToolSelected toolSelected, ViewportObserver viewport) {
        var o1 = menuSelected.distinctUntilChanged().map(Objects::nonNull);
        var o2 = toolSelected.distinctUntilChanged().map(Objects::nonNull);
        Observable.merge(o1, o2).subscribe(s -> {
            if (mobile) next(DrawerState.HIDE);
            else next(DrawerState.COLLAPSE);
        });
        viewport.isMobile().subscribe(isMobile -> {
            this.mobile = isMobile;
            if (isMobile) next(DrawerState.HIDE);
            else next(DrawerState.COLLAPSE);
        });
    }

    /** 모바일 환경에서 Drawer를 오버레이로 열거나 닫는다. */
    public void toggleOverlay() {
        if (!mobile) return;
        if (getValue() == DrawerState.OVERLAY) next(DrawerState.HIDE);
        else next(DrawerState.OVERLAY);
    }

    public boolean isMobile() { return mobile; }
}

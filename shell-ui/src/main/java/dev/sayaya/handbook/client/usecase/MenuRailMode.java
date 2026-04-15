package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.MenuRailState.*;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * Menu Rail 상태를 관리한다.
 * 드로어가 접혔을 때 하위 도구가 있으면 Menu Rail을 숨기고 Tool Rail만 표시한다.
 * 모바일에서는 DrawerState 와 무관하게 항상 BOTTOM_NAV 로 고정되어 하단 네비게이션 바로 상주한다.
 * 오버레이 drawer 는 secondary UI 이며, primary nav 진입점은 언제나 bottom-nav.
 */
@Singleton
public class MenuRailMode {
    @Delegate private final BehaviorSubject<MenuRailState> _this = behavior(HIDE);
    private boolean mobile;
    @Inject MenuRailMode(DrawerMode drawerMode, ToolList toolList, ViewportObserver viewport) {
        viewport.isMobile().subscribe(isMobile -> {
            this.mobile = isMobile;
            update(drawerMode.getValue(), toolList.getValue().size() <= 1);
        });
        drawerMode.subscribe(drawerState -> update(drawerState, toolList.getValue().size() <= 1));
    }
    private void update(DrawerState drawerState, boolean hasNoChildren) {
        if (mobile) {
            next(BOTTOM_NAV);
            return;
        }
        switch (drawerState) {
            case EXPAND -> next(EXPAND);
            case HIDE -> next(HIDE);
            case COLLAPSE -> next(hasNoChildren ? COLLAPSE : HIDE);
            case OVERLAY -> next(BOTTOM_NAV);
        }
    }
}

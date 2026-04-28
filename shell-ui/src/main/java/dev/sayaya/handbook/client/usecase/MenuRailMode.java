package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.DrawerState;
import dev.sayaya.handbook.domain.MenuRailState;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.domain.MenuRailState.*;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * Menu Rail 의 가시성 상태를 관리한다.
 * <p>
 * 모바일/데스크톱 레이아웃 차이는 이 상태 머신이 아니라 {@code .rail[mobile]} CSS 속성이
 * 담당한다. 여기서는 {@link MenuRailState} 세 값(EXPAND/COLLAPSE/HIDE) 만 사용한다.
 * <p>
 * 모바일 드릴인 로직: 도구가 2개 이상일 때 MenuRail 은 HIDE 되어 ToolRail 에게 하단 바
 * 자리를 양보한다. 도구가 1개 이하이면 MenuRail 이 EXPAND 상태로 하단 바를 차지한다.
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
        toolList.subscribe(tools -> update(drawerMode.getValue(), tools.size() <= 1));
    }
    private void update(DrawerState drawerState, boolean hasNoChildren) {
        if (mobile) {
            // 드릴인: 도구가 여러 개면 ToolRail 이 하단 바를 차지하도록 MenuRail 을 HIDE.
            next(hasNoChildren ? EXPAND : HIDE);
            return;
        }
        switch (drawerState) {
            case EXPAND -> next(EXPAND);
            case HIDE -> next(HIDE);
            case COLLAPSE -> next(hasNoChildren ? COLLAPSE : HIDE);
            case OVERLAY -> next(EXPAND);
        }
    }
}

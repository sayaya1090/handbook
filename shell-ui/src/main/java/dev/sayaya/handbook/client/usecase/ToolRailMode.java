package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.DrawerState;
import dev.sayaya.handbook.domain.MenuRailState;
import dev.sayaya.handbook.domain.ToolRailState;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.domain.ToolRailState.*;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * Tool Rail 의 가시성 상태를 관리한다.
 * <p>
 * 모바일/데스크톱 레이아웃 차이는 {@code .rail[mobile]} CSS 속성이 담당하므로 이 상태
 * 머신은 EXPAND/COLLAPSE/HIDE 만 사용한다.
 * <p>
 * 모바일 드릴인: 도구가 2개 이상이면 EXPAND 상태로 하단 바를 차지한다.
 */
@Singleton
public class ToolRailMode {
    @Delegate private final BehaviorSubject<ToolRailState> _this = behavior(HIDE);
    private boolean mobile;
    @Inject ToolRailMode(DrawerMode drawerMode, MenuRailMode menuMode, ToolList toolList, ViewportObserver viewport) {
        viewport.isMobile().subscribe(isMobile -> {
            this.mobile = isMobile;
            update(drawerMode.getValue(), menuMode.getValue(), toolList.getValue().size() > 1);
        });
        drawerMode.subscribe(drawerState -> update(drawerState, menuMode.getValue(), toolList.getValue().size() > 1));
        toolList.subscribe(tools -> update(drawerMode.getValue(), menuMode.getValue(), tools.size() > 1));
    }
    private void update(DrawerState drawerState, MenuRailState menuState, boolean hasMultipleChildren) {
        if (mobile) {
            next(hasMultipleChildren ? EXPAND : HIDE);
            return;
        }
        switch (drawerState) {
            case HIDE -> next(HIDE);
            case EXPAND -> next(hasMultipleChildren ? EXPAND : HIDE);
            case OVERLAY -> next(hasMultipleChildren ? EXPAND : HIDE);
            case COLLAPSE -> {
                if(menuState == MenuRailState.COLLAPSE) next(HIDE);
                else next(hasMultipleChildren ? COLLAPSE : HIDE);
            }
        }
    }
}

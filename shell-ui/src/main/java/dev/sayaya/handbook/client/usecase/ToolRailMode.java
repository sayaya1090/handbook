package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.ToolRailState.*;
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
        menuMode.subscribe(menuState -> update(drawerMode.getValue(), menuState, toolList.getValue().size() > 1));
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
                // 2026-05-05: peeking 지원. 
                // 드로어가 접혀있더라도 호버 등에 의해 도구가 생겼다면(hasMultipleChildren) 
                // 해당 도구들을 노출(EXPAND)한다.
                if (hasMultipleChildren) next(EXPAND);
                else next(HIDE);
            }
        }
    }
}

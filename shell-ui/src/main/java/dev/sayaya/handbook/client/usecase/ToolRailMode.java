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
 * Tool Rail 상태를 관리한다.
 * 드로어 상태와 도구 개수에 따라 Expand/Collapse/Hide를 결정한다.
 * 모바일에서는 DrawerState 와 무관하게 도구가 2개 이상이면 HORIZONTAL_CHIPS, 아니면 HIDE.
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
            next(hasMultipleChildren ? HORIZONTAL_CHIPS : HIDE);
            return;
        }
        switch (drawerState) {
            case HIDE -> next(HIDE);
            case EXPAND -> next(hasMultipleChildren ? EXPAND : HIDE);
            case OVERLAY -> next(hasMultipleChildren ? HORIZONTAL_CHIPS : HIDE);
            case COLLAPSE -> {
                if(menuState == MenuRailState.COLLAPSE) next(HIDE);
                else next(hasMultipleChildren ? COLLAPSE : HIDE);
            }
        }
    }
}

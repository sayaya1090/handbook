package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.ToolRailState.*;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * Tool Rail 상태를 관리한다.
 * 드로어 상태와 도구 개수에 따라 Expand/Collapse/Hide를 결정한다.
 * 모바일(OVERLAY)에서는 HORIZONTAL_CHIPS로 전환한다.
 */
@Singleton
public class ToolRailMode {
    @Delegate private final BehaviorSubject<ToolRailState> _this = behavior(HIDE);
    @Inject ToolRailMode(DrawerMode drawerMode, MenuRailMode menuMode, ToolList toolList) {
        drawerMode.subscribe(drawerState -> update(drawerState, menuMode.getValue(), toolList.getValue().size() > 1));
        toolList.subscribe(tools -> update(drawerMode.getValue(), menuMode.getValue(), tools.size() > 1));
    }
    private void update(DrawerState drawerState, MenuRailState menuState, boolean hasMultipleChildren) {
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

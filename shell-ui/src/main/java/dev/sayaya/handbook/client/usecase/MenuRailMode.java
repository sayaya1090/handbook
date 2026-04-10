package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.MenuRailState.*;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * Menu Rail 상태를 관리한다.
 * 드로어가 접혔을 때 하위 도구가 있으면 Menu Rail을 숨기고 Tool Rail만 표시한다.
 * 모바일(OVERLAY)에서는 BOTTOM_NAV로 전환한다.
 */
@Singleton
public class MenuRailMode {
    @Delegate private final BehaviorSubject<MenuRailState> _this = behavior(HIDE);
    @Inject MenuRailMode(DrawerMode drawerMode, ToolList toolList) {
        drawerMode.subscribe(drawerState -> update(drawerState, toolList.getValue().size() <= 1));
    }
    private void update(DrawerState drawerState, boolean hasNoChildren) {
        switch (drawerState) {
            case EXPAND -> next(EXPAND);
            case HIDE -> next(HIDE);
            case COLLAPSE -> next(hasNoChildren ? COLLAPSE : HIDE);
            case OVERLAY -> next(BOTTOM_NAV);
        }
    }
}

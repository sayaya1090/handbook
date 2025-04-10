package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.MenuRailState.*;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/*
 * 드로어 상태에 반응하여 메뉴 상태를 업데이트한다.
 * 드로어가 접혔을 때에는 선택된 메뉴의 하위 메뉴가 있는지 여부에 따라 메뉴 상태를 업데이트한다.
 *   - 하위 메뉴가 존재할 때: 스스로를 숨기고 하위 메뉴만 보이게끔 한다
 *   - 하위 메뉴가 없을 때: 접힌 상태를 출력
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
        }
    }
}

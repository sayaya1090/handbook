package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.ToolRailState.*;
import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/*
 * 드로어 상태에 반응하여 메뉴 상태를 업데이트한다.
 * 드로어가 숨겨짐: 숨김
 * 확장: 항목이 2개 이상 -> 확장, 아니면 -> 숨김
 * 축소: 항목이 2개 이상 -> 축소, 아니면 -> 숨김
 */
@Singleton
public class ToolRailMode {
    @Delegate private final BehaviorSubject<ToolRailState> _this = behavior(HIDE);
    @Inject ToolRailMode(DrawerMode drawerMode, ToolList toolList) {
        drawerMode.subscribe(drawerState -> update(drawerState, toolList.getValue().size() > 1));
    }
    private void update(DrawerState drawerState, boolean hasMultipleChildren) {
        switch (drawerState) {
            case HIDE -> next(HIDE);
            case EXPAND -> next(hasMultipleChildren ? EXPAND : HIDE);
            case COLLAPSE -> next(hasMultipleChildren ? COLLAPSE : HIDE);
        }
    }
}
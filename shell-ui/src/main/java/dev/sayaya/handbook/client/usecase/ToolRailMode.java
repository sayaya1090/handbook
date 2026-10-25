package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.MenuRailState;
import dev.sayaya.handbook.client.domain.ToolRailState;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.Operator;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.Js;
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
    private final MenuHover menuHover;

    @Inject ToolRailMode(DrawerMode drawerMode, MenuRailMode menuMode, ToolList toolList, MenuHover hover, ViewportObserver viewport) {
        this.menuHover = hover;
        // sayaya-rx 2.4.0 combineLatest: 다섯 소스(모두 BehaviorSubject) 중 하나가 바뀔 때마다 최신 값 묶음으로 재계산
        Operator.combineLatest(viewport.isMobile(), drawerMode.asObservable(), menuMode.asObservable(),
                toolList.asObservable(), hover.asObservable()).subscribe(v -> {
            this.mobile = Js.<Boolean>uncheckedCast(v.getAt(0));
            DrawerState drawerState = Js.uncheckedCast(v.getAt(1));
            MenuRailState menuState = Js.uncheckedCast(v.getAt(2));
            java.util.List<?> tools = Js.uncheckedCast(v.getAt(3));
            update(drawerState, menuState, tools.size() > 1, v.getAt(4) != null);
        });
    }
    private void update(DrawerState drawerState, MenuRailState menuState, boolean hasMultipleChildren, boolean isHovering) {
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
                // 드로어가 접혀있을 때 호버 탐색 중이면 EXPAND (라벨 노출), 
                // 호버가 아닌 선택된 상태라면 COLLAPSE (아이콘만 노출) 하여 UX 단순화.
                if (!hasMultipleChildren) next(HIDE);
                else if (isHovering) next(EXPAND);
                else next(COLLAPSE);
            }
        }
    }
}

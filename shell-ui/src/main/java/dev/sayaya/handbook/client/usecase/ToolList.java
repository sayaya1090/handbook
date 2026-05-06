package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;

/**
 * 선택/호버된 메뉴의 도구 목록을 관리한다.
 *
 * <p><b>주의:</b> {@link MenuHover} 가 2026-05-05 부터 다시 활성화되었습니다. 
 * 이로써 데스크톱 "아이콘만 보기(COLLAPSE)" 상태에서도 호버 시 도구 목록이 즉시 peek 되는 
 * UX 가 지원됩니다. 단, 사용자가 명시적으로 닫기 버튼을 누른 경우의 예외 처리는 
 * {@link ToolRailMode} 가 담당합니다.</p>
 */
@Singleton
public class ToolList {
    @Delegate private final BehaviorSubject<List<Tool>> _this = behavior(List.of());
    private final MenuSelected menuSelected;
    private final MenuHover menuHover;

    @Inject ToolList(MenuSelected menu, MenuHover hover, dev.sayaya.handbook.usecase.ToolProvider toolProvider) {
        this.menuSelected = menu;
        this.menuHover = hover;
        
        // 1. 내부 메뉴(선택/호버) 변경 감지
        menu.subscribe(m -> update(toolProvider));
        hover.subscribe(h -> update(toolProvider));
        
        // 2. 외부 모듈로부터의 도구 목록 수신 (ToolProvider 브릿지)
        // 외부 목록이 들어오면 이를 최우선으로 반영한다.
        toolProvider.tools().subscribe(tools -> {
            if (tools != null) {
                List<Tool> list = Arrays.asList(tools);
                next(list);
            }
        });
        toolProvider.subscribe(tools -> {});
    }

    private void update(dev.sayaya.handbook.usecase.ToolProvider toolProvider) {
        Menu h = menuHover.getValue();
        Menu m = menuSelected.getValue();
        Menu active = h != null ? h : m;
        List<Tool> list = fromMenu(active);
        next(list);
        // 외부에도 현재 활성화된 도구 목록을 알림 (동기화)
        toolProvider.publish(list.toArray(new Tool[0]));
    }

    private List<Tool> fromMenu(Menu menu) {
        if(menu == null) return List.of();
        var tools = menu.tools();
        return tools != null
            ? Arrays.stream(tools).sorted(nullsLast(comparing(Tool::order))).collect(Collectors.toList())
            : List.of();
    }
}

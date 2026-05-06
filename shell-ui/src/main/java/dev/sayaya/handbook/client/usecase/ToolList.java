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
    private List<Tool> externalTools = List.of();

    @Inject ToolList(MenuSelected menu, MenuHover hover, dev.sayaya.handbook.usecase.ToolProvider toolProvider) {
        this.menuSelected = menu;
        this.menuHover = hover;

        // 1. 내부 메뉴(선택/호버) 변경 감지
        menu.subscribe(m -> {
            update();
            // 선택된 메뉴의 도구만 외부 브릿지로 발행한다 (Peeking 도구는 발행하지 않음)
            List<Tool> tools = fromMenu(m);
            toolProvider.publish(tools.toArray(new Tool[0]));
        });
        hover.subscribe(h -> update());

        // 2. 외부 모듈로부터의 도구 목록 수신 (ToolProvider 브릿지)
        toolProvider.tools().subscribe(tools -> {
            this.externalTools = tools != null ? Arrays.asList(tools) : List.of();
            update();
        });
        toolProvider.subscribe(tools -> {});
    }

    private void update() {
        Menu h = menuHover.getValue();
        // 호버 중이면 호버 메뉴의 도구를, 아니면 선택된 메뉴의 도구 또는 외부 도구를 표시한다.
        List<Tool> list;
        if (h != null) list = fromMenu(h);
        else {
            Menu m = menuSelected.getValue();
            list = !externalTools.isEmpty() ? externalTools : fromMenu(m);
        }
        next(list);
    }

    private List<Tool> fromMenu(Menu menu) {
        if(menu == null) return List.of();
        var tools = menu.tools();
        return tools != null
            ? Arrays.stream(tools).sorted(nullsLast(comparing(Tool::order))).collect(Collectors.toList())
            : List.of();
    }
}

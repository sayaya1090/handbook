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
 */
@Singleton
public class ToolList {
    @Delegate private final BehaviorSubject<List<Tool>> _this = behavior(List.of());
    @Inject ToolList(MenuSelected menu, MenuHover hover) {
        Observable.merge(
            menu.asObservable(),
            hover.asObservable()
        ).distinctUntilChanged().subscribe(this::update);
    }
    private void update(Menu menu) {
        if(menu == null) next(List.of());
        else {
            var tools = menu.tools();
            List<Tool> list = tools != null
                ? Arrays.stream(tools).sorted(nullsLast(comparing(Tool::order))).collect(Collectors.toList())
                : List.of();
            next(list);
        }
    }
}

package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 현재 선택된 메뉴를 관리한다.
 * 메뉴에 도구가 하나뿐이면 자동으로 도구를 선택한다.
 */
@Singleton
public class MenuSelected {
    @Delegate private final BehaviorSubject<Menu> _this = behavior(null);
    @Inject MenuSelected(ToolSelected tool) {
        _this.distinctUntilChanged().subscribe(selected -> {
            if(selected != null && selected.tools() != null && selected.tools().length == 1)
                tool.next(selected.tools()[0]);
        });
    }
}

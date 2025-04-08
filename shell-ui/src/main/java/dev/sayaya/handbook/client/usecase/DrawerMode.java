package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/*
 * 메뉴나 툴이 변경되면 드로어를 자동으로 접는다
 */
@Singleton
public class DrawerMode {
    @Delegate private final BehaviorSubject<DrawerState> _this = behavior(DrawerState.EXPAND);
    @Inject DrawerMode(MenuSelected menuSelected, ToolSelected toolSelected) {
        var o1 = menuSelected.distinctUntilChanged().map(Objects::nonNull);
        var o2 = toolSelected.distinctUntilChanged().map(Objects::nonNull);
        Observable.merge(o1, o2).subscribe(s->next(DrawerState.COLLAPSE));
    }
}

package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/*
 * 메뉴가 변경되면 드로어를 자동으로 접는다
 */
@Singleton
public class DrawerMode {
    @Delegate private final BehaviorSubject<DrawerState> _this = behavior(DrawerState.EXPAND);
    @Inject DrawerMode(MenuSelected menuSelected) {
        menuSelected.distinctUntilChanged().subscribe(s->next(DrawerState.COLLAPSE));
    }
}

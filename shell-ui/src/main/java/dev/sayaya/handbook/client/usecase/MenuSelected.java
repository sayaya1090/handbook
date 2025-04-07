package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class MenuSelected {
    @Delegate private final BehaviorSubject<Menu> _this = behavior(null);
    @Inject MenuSelected() {}
}

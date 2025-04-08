package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuHover;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class MenuHoverElementProvider {
    @Delegate private final BehaviorSubject<MenuRailItemElement> _this = behavior(null);
    @Inject MenuHoverElementProvider(MenuHover menu) {}
}

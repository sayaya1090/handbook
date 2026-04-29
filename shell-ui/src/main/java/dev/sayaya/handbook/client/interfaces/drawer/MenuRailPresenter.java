package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.MenuList;
import dev.sayaya.handbook.client.usecase.MenuRailMode;
import dev.sayaya.handbook.usecase.ViewportObserver;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MenuRailPresenter {
    @Inject
    public MenuRailPresenter(MenuRailElement view, MenuList list, MenuRailMode mode, ViewportObserver viewport) {
        list.distinctUntilChanged().subscribe(view::update);
        mode.distinctUntilChanged().subscribe(view::setMode);
        viewport.isMobile().subscribe(view::setMobile);
    }
}

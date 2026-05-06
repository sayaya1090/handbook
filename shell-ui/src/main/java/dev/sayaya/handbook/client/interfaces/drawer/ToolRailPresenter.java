package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.ToolList;
import dev.sayaya.handbook.client.usecase.ToolRailMode;
import dev.sayaya.handbook.usecase.ViewportObserver;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ToolRailPresenter {
    @Inject
    public ToolRailPresenter(ToolRailElement view, ToolList list, ToolRailMode mode, ViewportObserver viewport, MenuSelectedElementProvider anchor) {
        list.distinctUntilChanged().subscribe(view::update);
        mode.distinctUntilChanged().subscribe(view::setMode);
        viewport.isMobile().subscribe(view::setMobile);
        anchor.distinctUntilChanged().subscribe(view::offset);
    }
}

package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.DrawerMode;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DrawerPresenter {
    @Inject
    public DrawerPresenter(DrawerElement view, DrawerMode mode) {
        view.onOverlayClick(mode::toggleOverlay);
        mode.subscribe(view::setState);
    }
}

package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.client.interfaces.drawer.DrawerPresenter;
import dev.sayaya.handbook.client.interfaces.drawer.MenuRailPresenter;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsPresenter;
import dev.sayaya.handbook.client.interfaces.drawer.ShellAppBarElement;
import dev.sayaya.handbook.client.interfaces.drawer.ToolRailPresenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.body;

@Singleton
public class UiInitializer {
    private final ShellAppBarElement appBar;
    private final MobileTabsElement mobileTabs;
    @SuppressWarnings("unused") private final MobileTabsPresenter mobileTabsPresenter;
    @SuppressWarnings("unused") private final DrawerPresenter drawerPresenter;
    @SuppressWarnings("unused") private final MenuRailPresenter menuRailPresenter;
    @SuppressWarnings("unused") private final ToolRailPresenter toolRailPresenter;
    private final ProgressElement progressElement;
    private final ContentElement contentElement;

    @Inject
    public UiInitializer(
            ShellAppBarElement appBar,
            MobileTabsElement mobileTabs,
            MobileTabsPresenter mobileTabsPresenter,
            DrawerPresenter drawerPresenter,
            MenuRailPresenter menuRailPresenter,
            ToolRailPresenter toolRailPresenter,
            ProgressElement progressElement,
            ContentElement contentElement
    ) {
        this.appBar = appBar;
        this.mobileTabs = mobileTabs;
        this.mobileTabsPresenter = mobileTabsPresenter;
        this.drawerPresenter = drawerPresenter;
        this.menuRailPresenter = menuRailPresenter;
        this.toolRailPresenter = toolRailPresenter;
        this.progressElement = progressElement;
        this.contentElement = contentElement;
    }

    public void initialize() {
        body().add(appBar);
        body().add(mobileTabs);
        body().add(progressElement);
        body().add(contentElement);
    }
}


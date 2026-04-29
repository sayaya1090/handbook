package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.ShellModule;
import dev.sayaya.handbook.client.interfaces.drawer.*;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ShellModule.class, DrawerMock.class })
public interface Component {
    DrawerElement drawer();
    DrawerPresenter drawerPresenter();
    MenuRailPresenter menuRailPresenter();
    ToolRailPresenter toolRailPresenter();
    ShellAppBarElement shellAppBar();
    MobileTabsElement mobileTabs();
    MobileTabsPresenter mobileTabsPresenter();
    ModuleScriptManager script();
    HistoryManager historyManager();
    UrlBasedMenuResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();
    WorkspaceOnboardingBootstrapper bootstrapper();
    UriStore uri();
    ProgressStore progress();
}


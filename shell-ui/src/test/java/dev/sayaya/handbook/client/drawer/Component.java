package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsElement;
import dev.sayaya.handbook.client.interfaces.drawer.MobileTabsPresenter;
import dev.sayaya.handbook.client.interfaces.drawer.ShellAppBarElement;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.ModuleScriptManager;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.WorkspaceOnboardingBootstrapper;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { DrawerMock.class })
public interface Component {
    DrawerElement drawer();
    ShellAppBarElement shellAppBar();
    MobileTabsElement mobileTabs();
    MobileTabsPresenter mobileTabsPresenter();
    ModuleScriptManager script();
    HistoryManager historyManager();
    UrlBasedMenuResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();
    WorkspaceOnboardingBootstrapper bootstrapper();
    BehaviorSubject<String> uri();
    Observer<Progress> progressObserver();
}

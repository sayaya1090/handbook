package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.interfaces.ModuleScriptManager;
import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.HostSharedModule;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedToolResolver;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { HostSharedModule.class, DrawerMock.class })
public interface Component {
    DrawerElement drawer();
    ModuleScriptManager script();
    HistoryManager historyManager();
    UrlBasedToolResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();

    BehaviorSubject<String> uri();
    Observable<Tool[]> tools();
}

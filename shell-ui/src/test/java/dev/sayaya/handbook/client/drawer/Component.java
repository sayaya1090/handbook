package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.interfaces.ModuleScriptManager;
import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { HostSharedModule.class, ClientSharedModule.class, DrawerMock.class })
public interface Component {
    DrawerElement drawer();
    ModuleScriptManager script();
    HistoryManager historyManager();
    UrlBasedToolResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();

    BehaviorSubject<String> uri();
    Observable<Tool[]> tools();
}

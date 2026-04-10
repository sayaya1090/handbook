package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.ModuleScriptManager;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedMenuResolver;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { DrawerMock.class })
public interface Component {
    DrawerElement drawer();
    ModuleScriptManager script();
    HistoryManager historyManager();
    UrlBasedMenuResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();
    BehaviorSubject<String> uri();
}

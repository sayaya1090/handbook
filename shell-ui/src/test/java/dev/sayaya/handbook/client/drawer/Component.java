package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.interfaces.ModuleScriptElement;
import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.HostSharedModule;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedToolResolver;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { HostSharedModule.class, DrawerMock.class })
public interface Component {
    DrawerElement drawer();
    ModuleScriptElement script();
    HistoryManager historyManager();
    UrlBasedToolResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();
}

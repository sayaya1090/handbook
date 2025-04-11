package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.interfaces.ModuleScriptElement;
import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.rx.Observable;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@dagger.Component(modules = { HostSharedModule.class, ClientSharedModule.class, DrawerMock.class })
public interface Component {
    DrawerElement drawer();
    ModuleScriptElement script();
    HistoryManager historyManager();
    UrlBasedToolResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();

    Observable<List<Tool>> tools();
}

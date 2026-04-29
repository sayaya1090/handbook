package dev.sayaya.handbook.client.history;

import dagger.Component;
import dev.sayaya.handbook.client.ShellModule;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.UriStore;

import javax.inject.Singleton;

@Singleton
@Component(modules = { ShellModule.class })
interface TestComponent {
    HistoryManager historyManager();
    UriStore uri();
    MenuSelected menuSelected();
}

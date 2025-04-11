package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.ModuleScriptElement;
import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.HostSharedModule;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedToolResolver;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { Module.class, ApiModule.class, HostSharedModule.class })
public interface Component {
    ModuleScriptElement scriptElement();
    ProgressElement progressElement();
    ContentElement contentElement();
    HistoryManager historyManager();
    FrameUpdater frameUpdater();
    UrlBasedToolResolver urlBasedToolResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();
}

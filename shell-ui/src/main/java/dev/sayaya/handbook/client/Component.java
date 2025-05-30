package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.*;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.*;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { Module.class, ApiModule.class, HostSharedModule.class })
public interface Component {
    ModuleScriptManager scriptManager();
    FontElement fontElement();
    FontStyleElement fontStyleElement();
    ProgressElement progressElement();
    ContentElement contentElement();
    HistoryManager historyManager();
    FrameUpdater frameUpdater();
    UrlBasedMenuResolver urlBasedMenuResolver();
    ToolBasedMenuResolver toolBasedMenuResolver();
    LanguagePackManager languagePackManager();
    WorkspaceRequireHandler workspaceRequireHandler();
}

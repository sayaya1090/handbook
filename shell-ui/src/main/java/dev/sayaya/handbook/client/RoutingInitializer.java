package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.ModuleScriptManager;
import dev.sayaya.handbook.client.usecase.ToolBasedMenuResolver;
import dev.sayaya.handbook.client.usecase.UrlBasedMenuResolver;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RoutingInitializer {
    private final HistoryManager historyManager;
    private final UrlBasedMenuResolver urlBasedMenuResolver;
    private final ToolBasedMenuResolver toolBasedMenuResolver;
    private final FrameUpdater frameUpdater;
    private final ModuleScriptManager scriptManager;

    @Inject
    public RoutingInitializer(
            HistoryManager historyManager,
            UrlBasedMenuResolver urlBasedMenuResolver,
            ToolBasedMenuResolver toolBasedMenuResolver,
            FrameUpdater frameUpdater,
            ModuleScriptManager scriptManager
    ) {
        this.historyManager = historyManager;
        this.urlBasedMenuResolver = urlBasedMenuResolver;
        this.toolBasedMenuResolver = toolBasedMenuResolver;
        this.frameUpdater = frameUpdater;
        this.scriptManager = scriptManager;
    }

    public void initialize() {
        historyManager.initialize();
        urlBasedMenuResolver.initialize();
        toolBasedMenuResolver.initialize();
        frameUpdater.initialize();
        scriptManager.initialize();
    }
}

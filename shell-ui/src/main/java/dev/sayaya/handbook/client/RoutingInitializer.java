package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.frame.FrameUpdater;
import dev.sayaya.handbook.client.usecase.*;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RoutingInitializer {
    private final HistoryManager historyManager;
    private final UrlBasedMenuResolver urlBasedMenuResolver;
    private final ToolBasedMenuResolver toolBasedMenuResolver;
    private final FrameUpdater frameUpdater;
    private final ModuleScriptManager scriptManager;
    private final HomeRedirector homeRedirector;

    @Inject
    public RoutingInitializer(
            HistoryManager historyManager,
            UrlBasedMenuResolver urlBasedMenuResolver,
            ToolBasedMenuResolver toolBasedMenuResolver,
            FrameUpdater frameUpdater,
            ModuleScriptManager scriptManager,
            HomeRedirector homeRedirector
    ) {
        this.historyManager = historyManager;
        this.urlBasedMenuResolver = urlBasedMenuResolver;
        this.toolBasedMenuResolver = toolBasedMenuResolver;
        this.frameUpdater = frameUpdater;
        this.scriptManager = scriptManager;
        this.homeRedirector = homeRedirector;
    }

    public void initialize() {
        historyManager.initialize();
        urlBasedMenuResolver.initialize();
        toolBasedMenuResolver.initialize();
        frameUpdater.initialize();
        scriptManager.initialize();
        homeRedirector.initialize();
    }
}

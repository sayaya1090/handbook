package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.historyManager().initialize();
        components.urlBasedMenuResolver().initialize();
        components.toolBasedMenuResolver().initialize();
        components.frameUpdater().initialize();
        components.languagePackManager().initialize();
        components.scriptManager().initialize();
        components.workspaceRequireHandler().initialize();
        document.head.append(components.fontElement().element());
        document.head.append(components.fontStyleElement().element());
        body().add(components.progressElement())
              .add(components.contentElement());
    }
}

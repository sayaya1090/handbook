package dev.sayaya.handbook.client.drawer;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.*;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.historyManager().initialize();
        components.urlBasedToolResolver().initialize();
        components.toolBasedMenuResolver().initialize();
        body().style("display: flex; height: -webkit-fill-available; inset: 0;")
                .add(components.script())
                .add(components.drawer())
                .add(div().css("frame").style("display: flex; align-items: center; gap: 10px; margin: 10px; left: 600px;")
                        .add(button("URL 1").id("url1")
                                .on(EventType.click, evt-> components.uri().next("menu1-tool1")))
                        .add(button("URL 2").id("url2")
                                .on(EventType.click, evt-> components.uri().next("menu3-tool1")))
                        .add(button("URL 2").id("url3")
                                .on(EventType.click, evt-> components.uri().next("menu3-tool2")))
                );
        components.tools().subscribe(tools->{
            tools.stream().filter(tool->tool.title().equals("menu1-tool1")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Menu1 Tool1 Clicked")));
            tools.stream().filter(tool->tool.title().equals("menu2-tool1")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Menu2 Tool1 Clicked")));
            tools.stream().filter(tool->tool.title().equals("menu2-tool2")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Menu2 Tool2 Clicked")));
            tools.stream().filter(tool->tool.title().equals("menu3-tool1")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Menu3 Tool1 Clicked")));
            tools.stream().filter(tool->tool.title().equals("menu3-tool2")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Menu3 Tool2 Clicked")));
        });
    }
}

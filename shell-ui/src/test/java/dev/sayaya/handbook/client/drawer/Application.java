package dev.sayaya.handbook.client.drawer;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.usecase.ClientWindow;
import elemental2.dom.DomGlobal;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        body().style("display: flex; height: -webkit-fill-available; inset: 0;")
                .add(components.script())
                .add(components.drawer());
        ClientWindow.tools.asObservable().subscribe(tools->{
            tools.stream().filter(tool->tool.title.equals("menu1-tool1")).findFirst().ifPresent(tool->tool.function = ()-> DomGlobal.console.log("Menu1 Tool1 Clicked"));
            tools.stream().filter(tool->tool.title.equals("menu2-tool1")).findFirst().ifPresent(tool->tool.function = ()-> DomGlobal.console.log("Menu2 Tool1 Clicked"));
            tools.stream().filter(tool->tool.title.equals("menu2-tool2")).findFirst().ifPresent(tool->tool.function = ()-> DomGlobal.console.log("Menu2 Tool2 Clicked"));
            tools.stream().filter(tool->tool.title.equals("menu3-tool1")).findFirst().ifPresent(tool->tool.function = ()-> DomGlobal.console.log("Menu3 Tool1 Clicked"));
        });
    }
}

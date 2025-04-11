package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        DomGlobal.console.log("Application started");
        DomGlobal.console.log(components);
        DomGlobal.console.log(components.canvas());
        DomGlobal.console.log(components.renderer());
        DomGlobal.console.log(components.tools());
        DomGlobal.console.log(components.progress());
        DomGlobal.console.log(components.uri());

        components.renderer().next(frame-> {
            DomGlobal.console.log(frame);
            frame.append(components.canvas().element());
            return true;
        });
        components.tools().subscribe(tools ->{
            DomGlobal.console.log("Tools: " + tools);
            tools.stream().filter(tool->tool.title().equals("reload")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Reload")));
            tools.stream().filter(tool->tool.title().equals("save")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Save")));
            tools.stream().filter(tool->tool.title().equals("undo")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Undo")));
            tools.stream().filter(tool->tool.title().equals("redo")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Redo")));
        });
    }
}

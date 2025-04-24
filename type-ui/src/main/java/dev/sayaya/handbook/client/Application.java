package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLLinkElement;

import java.util.Arrays;

import static org.jboss.elemento.Elements.htmlElement;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.renderer().next(frame-> {
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/typebox.css").element());
            frame.append(htmlElement("link", HTMLLinkElement.class).attr("rel", "stylesheet").attr("href", "css/canvas.css").element());
            frame.append(components.controller().element());
            frame.append(components.canvas().element());
            return true;
        });
        components.tools().subscribe(tools ->{
            Arrays.stream(tools).filter(tool->tool.title().equals("reload")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Reload")));
            Arrays.stream(tools).filter(tool->tool.title().equals("save")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Save")));
            Arrays.stream(tools).filter(tool->tool.title().equals("undo")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Undo")));
            Arrays.stream(tools).filter(tool->tool.title().equals("redo")).findFirst().ifPresent(tool->tool.function(()-> DomGlobal.console.log("Redo")));
        });
    }
}

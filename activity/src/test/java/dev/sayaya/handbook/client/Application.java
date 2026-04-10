package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Menu;

import static elemental2.dom.DomGlobal.console;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        var menu = Menu.builder().order("DD").build();
        menu.order();
        console.log(        menu.order());
    }
}

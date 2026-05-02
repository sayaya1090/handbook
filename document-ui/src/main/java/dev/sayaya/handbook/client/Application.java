package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.usecase.AgentSearch;
import dev.sayaya.handbook.usecase.AgentState;
import dev.sayaya.handbook.usecase.RenderSharing;
import dev.sayaya.rx.Observable;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();

        component.documentEventHandler().init();
        component.agentDocumentHandler().init();
        body().add(component.toastContainer())
              .add(component.confirmDialog());

        RenderSharing.next((RenderSharing.NextFn) frame -> {
            elemental2.dom.HTMLElement el = jsinterop.base.Js.cast(frame);
            el.innerHTML = "";
            el.append(component.spreadsheetElement().element());
            el.append(component.pagination().element());
        });

        AgentState.register(component.documentStateProvider());
        AgentSearch.register(query -> Observable.of("{\"results\":[]}"));
    }
}

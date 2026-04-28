package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.handbook.usecase.RenderSharing;
import dev.sayaya.handbook.usecase.AgentSearch;
import dev.sayaya.handbook.usecase.AgentState;
import dev.sayaya.handbook.usecase.WorkspaceEvent;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        
        component.documentEventHandler().init();
        body().add(component.toastContainer());

        component.typeRepository().list(null).subscribe(types -> {
            if (types != null && !types.isEmpty()) {
                List<TypeValue> typeList = new ArrayList<>(types);
                component.typeList().next(typeList);
                component.typeProvider().next(typeList.get(0));
            }
        });

        RenderSharing.next(frame -> {
            frame.innerHTML = "";
            frame.append(component.spreadsheetElement().element());
            return true;
        });

        AgentState.register(component.documentStateProvider());
        AgentSearch.register(query -> "{\"results\":[]}");
    }
}

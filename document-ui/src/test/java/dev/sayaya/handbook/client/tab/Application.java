package dev.sayaya.handbook.client.tab;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.Workspace;

import java.util.stream.Collectors;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override public void onModuleLoad() {
        components.workspaceProvider().next(Workspace.builder().id("").name("").build());
        body().add(components.tabs());
        body().add(components.controller());
        body().add(div().style("""
                    width: calc(100vw - 2rem);
                    padding: 1rem;
                    height: fit-content;
                """).add(components.table()));
        components.typeRepository().list().subscribe(list->{
            var map = list.stream().collect(Collectors.groupingBy(Type::id, Collectors.toMap(Type::version, type -> type)));
            components.typeList().next(map);
        });
    }
}

package dev.sayaya.handbook.client.tab;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.Workspace;

import java.util.Date;
import java.util.List;
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
                    padding: 0 1rem;
                    height: fit-content;
                """).add(components.table()));
        components.typeRepository().list().subscribe(list->{
            var map = list.stream().collect(Collectors.groupingBy(Type::id, Collectors.toMap(Type::version, type -> type)));
            components.typeList().next(map);
        });
        components.documentList().set(
                Document.builder().id("A").type("TT").serial("1").effectDateTime(new Date()).expireDateTime(new Date())
                        .value("attr_4", true)
                        .value("attr_2", new String[] {"a", "a\\,55", "b"})
                        .value("attr_7", "Apple")
                        .build(),
                Document.builder().id("B").type("TT").serial("2").effectDateTime(new Date()).expireDateTime(new Date()).build(),
                Document.builder().id("C").type("TT").serial("3").effectDateTime(new Date()).expireDateTime(new Date()).build(),
                Document.builder().id("D").type("TT").serial("4").effectDateTime(new Date()).expireDateTime(new Date()).build(),
                Document.builder().id("E").type("TT").serial("5").effectDateTime(new Date()).expireDateTime(new Date()).build(),
                Document.builder().id("F").type("TT").serial("6").effectDateTime(new Date()).expireDateTime(new Date()).build(),
                Document.builder().id("G").type("TT").serial("7").effectDateTime(new Date()).expireDateTime(new Date()).build(),
                Document.builder().id("H").type("TT").serial("8").effectDateTime(new Date()).expireDateTime(new Date()).build());
    }
}

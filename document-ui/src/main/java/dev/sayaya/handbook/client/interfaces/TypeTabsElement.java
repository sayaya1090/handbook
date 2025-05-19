package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.ui.dom.MdTabsElement;
import dev.sayaya.ui.elements.TabsElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;

import static dev.sayaya.ui.elements.TabsElementBuilder.tabs;

@Singleton
public class TypeTabsElement implements IsElement<MdTabsElement> {
    @Delegate private TabsElementBuilder.TabsPrimaryElementBuilder tabs = tabs().primary().style("""
            min-width: -webkit-fill-available;
            width: fit-content;
            """);
    private final TypeProvider select;
    @Inject TypeTabsElement(TypeList types, TypeProvider select) {
        this.select = select;
        types.subscribe(this::update);
    }
    private void update(List<Type> types) {
        tabs.element().innerHTML = "";
        for (Type type : types) {
            tabs.tab().add(type.id()).on(EventType.click, evt-> select.next(type));
        }
    }
}

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

import java.util.Comparator;
import java.util.Map;

import static dev.sayaya.ui.elements.TabsElementBuilder.tabs;

@Singleton
public class TypeTabsElement implements IsElement<MdTabsElement> {
    @Delegate private TabsElementBuilder.TabsPrimaryElementBuilder tabs = tabs().primary().style("""
            min-width: -webkit-fill-available;
            width: fit-content;
            """).autoActivate();
    private final TypeProvider select;
    @Inject TypeTabsElement(TypeList types, TypeProvider select) {
        this.select = select;
        types.subscribe(this::update);
    }
    private void update(Map<String, Map<String, Type>> types) {
        tabs.element().innerHTML = "";
        String firstTypeId = null;
        for (var typeId : types.keySet()) {
            if (firstTypeId == null) firstTypeId = typeId;
            tabs.tab().add(typeId).active(firstTypeId.equals(typeId)).on(EventType.click, evt-> update(typeId, types));
        }
        if(firstTypeId!=null) update(firstTypeId, types);
    }
    private void update(String typeId, Map<String, Map<String, Type>> types) {
        if (select.getValue()!=null && select.getValue().id().equals(typeId)) return;
        var typeMap = types.get(typeId);
        if(typeMap==null || typeMap.isEmpty()) return;
        var nextVersion = typeMap.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .findFirst().orElse(null);
        select.next(typeMap.get(nextVersion));
    }
}

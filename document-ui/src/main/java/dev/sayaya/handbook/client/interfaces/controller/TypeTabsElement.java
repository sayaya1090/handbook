package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.TypeInfo;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/** 타입 탭 선택 컴포넌트. */
@Singleton
public class TypeTabsElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;
    private final TypeProvider typeProvider;

    @Inject
    public TypeTabsElement(TypeList typeList, TypeProvider typeProvider) {
        this.typeProvider = typeProvider;
        this.element = div().css("doc-type-tabs").element();
        typeList.subscribe(this::renderTabs);
    }

    private void renderTabs(List<TypeInfo> types) {
        element.innerHTML = "";
        if (types == null) return;
        for (TypeInfo type : types) {
            var tab = span().css("doc-type-tab").element();
            tab.textContent = type.id;
            tab.addEventListener("click", e -> typeProvider.next(type));
            element.appendChild(tab);
        }
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}

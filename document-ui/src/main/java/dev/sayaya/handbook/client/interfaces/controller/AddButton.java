package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.DocumentValue;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.AddDocumentAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import jsinterop.base.JsPropertyMap;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.button;

/** 새 문서 추가 버튼. */
@Singleton
public class AddButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;

    @Inject
    public AddButton(ActionManager actionManager, DocumentList documentList) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-add").element();
        element.textContent = "Add";
        element.addEventListener("click", e -> {
            DocumentValue newDoc = new DocumentValue();
            newDoc.data = JsPropertyMap.of();
            actionManager.execute(new AddDocumentAction(documentList, newDoc));
        });
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}

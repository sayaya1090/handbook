package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.DocumentRepository;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.button;

/** 변경사항 저장 버튼. */
@Singleton
public class SaveButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;

    @Inject
    public SaveButton(ActionManager actionManager, DocumentList documentList, DocumentRepository documentRepository) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-save").element();
        element.textContent = "Save";
        element.addEventListener("click", e ->
            documentRepository.save(documentList.getValue()).subscribe(v -> actionManager.clear())
        );
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}

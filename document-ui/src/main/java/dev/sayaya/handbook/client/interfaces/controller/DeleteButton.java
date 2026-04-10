package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.DeleteDocumentAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.usecase.LabelProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.button;

/** 선택 문서 삭제 버튼. */
@Singleton
public class DeleteButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;

    @Inject
    public DeleteButton(ActionManager actionManager, DocumentList documentList, LabelProvider labelProvider) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-delete").element();
        labelProvider.subscribe(labels -> element.textContent = labels.getOrDefault("document.delete", "Delete"));
        element.addEventListener("click", e -> {
            var docs = documentList.getValue();
            if (!docs.isEmpty()) {
                int lastIdx = docs.size() - 1;
                actionManager.execute(new DeleteDocumentAction(
                    documentList,
                    List.of(docs.get(lastIdx)),
                    List.of(lastIdx)
                ));
            }
        });
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}

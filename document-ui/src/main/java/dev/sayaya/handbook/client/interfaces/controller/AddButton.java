package dev.sayaya.handbook.client.interfaces.controller;
import dev.sayaya.handbook.domain.Document;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.usecase.AddDocumentAction;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;
import jsinterop.base.JsPropertyMap;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AddButton implements IsElement<HTMLElement> {
    @Delegate private final ButtonElementBuilder.TextButtonElementBuilder _this;
    private final TypeProvider typeProvider;

    @Inject
    public AddButton(ActionManager actionManager, DocumentList documentList, LabelProvider labelProvider, TypeProvider typeProvider) {
        this.typeProvider = typeProvider;
        this._this = ButtonElementBuilder.button().text().css("doc-ctrl-btn", "doc-ctrl-btn-add");
        labelProvider.subscribe(labels -> _this.text(labels.getOrDefault("document.add", "Add")));
        _this.onClick(e -> {
            Type type = typeProvider.getValue();
            if (type == null) return;
            Document newDoc = Document.create(null, type.id(), "serial");
            actionManager.execute(new AddDocumentAction(documentList, newDoc));
        });
    }
}

package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.interfaces.table.DocumentSelectedList;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdIconButtonElement;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

@Singleton
class RemoveButton implements IconButtonElementBuilder<MdIconButtonElement, IconButtonElementBuilder.PlainIconButtonElementBuilder> {
    @Delegate private final PlainIconButtonElementBuilder submit = button().icon().add(
            IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-minus")
    );
    private final ActionManager actionManager;
    private final DocumentSelectedList selections;
    @Inject RemoveButton(ActionManager actionManager, DocumentSelectedList selections, Observable<Label> labels) {
        this.actionManager = actionManager;
        this.selections = selections;
        labels.subscribe(this::update);
        selections.subscribe(list-> submit.element().disabled = list.isEmpty());
        submit.onClick(evt-> apply());
    }
    private void apply() {
        var documents = selections.getValue();
        actionManager.remove(documents);
        selections.clear();
    }
    private void update(Label label) {
        //submitLabel.element().innerHTML = findLabelOrDefault(label, "redo");
    }
}

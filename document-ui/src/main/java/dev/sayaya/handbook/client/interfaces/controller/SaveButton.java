package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.DocumentListToDelete;
import dev.sayaya.handbook.client.usecase.DocumentListToUpsert;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdButtonElement;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLLabelElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

import static dev.sayaya.handbook.client.domain.Label.findLabelOrDefault;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static org.jboss.elemento.Elements.label;

@Singleton
class SaveButton implements ButtonElementBuilder<MdButtonElement.MdFilledTonalButtonElement, ButtonElementBuilder.FilledTonalButtonElementBuilder> {
    private final HTMLContainerBuilder<HTMLLabelElement> submitLabel = label();
    @Delegate private final ButtonElementBuilder.FilledTonalButtonElementBuilder submit = button().filledTonal().icon(
            IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-cloud-arrow-up")
    ).add(submitLabel).style("margin-left: 1rem;");
    @Inject SaveButton(ActionManager actionManager, DocumentListToUpsert toUpdate, DocumentListToDelete toDelete, Observable<Label> labels) {
        labels.subscribe(this::update);
        submit.onClick(evt->actionManager.save());
        toUpdate.subscribe(s->update(s, toDelete.getValue()));
        toDelete.subscribe(s->update(toUpdate.getValue(), s));
    }
    private void update(Set<Document> toUpdate, Set<Document> toDelete) {
        submit.element().disabled = toUpdate.isEmpty() && toDelete.isEmpty();
    }
    private void update(Label label) {
        submitLabel.element().innerHTML = findLabelOrDefault(label, "save");
    }
}

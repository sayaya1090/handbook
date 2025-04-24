package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdButtonElement;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.Label.findLabelOrDefault;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static org.jboss.elemento.Elements.div;

@Singleton
class ReloadButton implements ButtonElementBuilder<MdButtonElement.MdOutlinedButtonElement, ButtonElementBuilder.OutlinedButtonElementBuilder> {
    private final HTMLContainerBuilder<HTMLDivElement> submitLabel = div();
    @Delegate private final OutlinedButtonElementBuilder submit = button().outlined().add(submitLabel);
    @Inject ReloadButton(Observable<Label> labels) {
        labels.subscribe(this::update);
        submit.onClick(evt-> {

        });
    }
    private void update(Label label) {
        submitLabel.element().innerHTML = findLabelOrDefault(label, "reload");
    }
}

package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdIconButtonElement;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

@Singleton
class RedoButton implements IconButtonElementBuilder<MdIconButtonElement, IconButtonElementBuilder.PlainIconButtonElementBuilder> {
    @Delegate private final PlainIconButtonElementBuilder submit = button().icon().add(
            IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-rotate-right")
    );
    @Inject RedoButton(Observable<Label> labels) {
        labels.subscribe(this::update);
    }
    private void update(Label label) {
        //submitLabel.element().innerHTML = findLabelOrDefault(label, "redo");
    }
}

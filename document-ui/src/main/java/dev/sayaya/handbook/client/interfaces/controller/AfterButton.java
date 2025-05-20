package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.ui.dom.MdIconButtonElement;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import lombok.experimental.Delegate;

import javax.inject.Inject;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

public class AfterButton implements IconButtonElementBuilder<MdIconButtonElement, IconButtonElementBuilder.PlainIconButtonElementBuilder> {
    @Delegate private final PlainIconButtonElementBuilder btn = button().icon().add(
            IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-caret-right")
    );
    @Inject AfterButton() {

    }
    private void update(Label label) {
        //submitLabel.element().innerHTML = findLabelOrDefault(label, "reload");
    }
}

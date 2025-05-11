package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Label;
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
public class BeforeButton implements IconButtonElementBuilder<MdIconButtonElement, IconButtonElementBuilder.PlainIconButtonElementBuilder> {
    @Delegate private final PlainIconButtonElementBuilder btn = button().icon().add(
            IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-caret-left")
    );
    @Inject BeforeButton(ActionManager mgr, BasetimeProvider basetime, Observable<Label> labels) {
        labels.subscribe(this::update);
        btn.onClick(evt->mgr.changeToBeforeLayout());
        basetime.subscribe(evt-> btn.element().disabled = !mgr.hasBeforeLayout());
    }
    private void update(Label label) {
        //submitLabel.element().innerHTML = findLabelOrDefault(label, "reload");
    }
}

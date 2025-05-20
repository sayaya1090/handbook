package dev.sayaya.handbook.client.interfaces.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.Event;
import jsinterop.base.Js;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

@Singleton
public class VersionElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined().css("label")
            .label("Version").value("1.0.0").enable(false);
    @Inject VersionElement() {
        ipt.on(EventType.change, this::update);
        //ipt.element().disabled = true;
        ipt.element().readOnly = true;
    }
    private void update(Event evt) {
        var ipt = Js.asPropertyMap(evt.target);
    }
    void update(Type type) {
        ipt.value(type.id());
    }
}

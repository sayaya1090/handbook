package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

@Singleton
public class VersionElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined().css("label").style("width: 8rem;")
            .label("Version").value("1.0.0").enable(false);
    @Inject VersionElement(TypeProvider typeProvider) {
        ipt.element().readOnly = true;
        typeProvider.subscribe(this::update);
    }
    void update(Type type) {
        if(type == null) ipt.value(null);
        else ipt.value(type.version());
    }
}

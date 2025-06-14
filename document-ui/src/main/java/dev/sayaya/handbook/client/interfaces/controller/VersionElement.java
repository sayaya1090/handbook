package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.Label.findLabelOrDefault;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

@Singleton
public class VersionElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined().css("label").style("width: 8rem;").value("1.0.0").enable(false);
    @Inject VersionElement(TypeProvider typeProvider, Observable<Label> labels) {
        labels.subscribe(this::update);
        ipt.element().readOnly = true;
        typeProvider.subscribe(this::update);
    }
    void update(Type type) {
        if(type == null) ipt.value(null);
        else ipt.value(type.version());
    }
    private void update(Label label) {
        ipt.label(findLabelOrDefault(label, "Version"));
    }
}

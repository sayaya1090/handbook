package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.validator.ValidatorRegex;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import java.util.List;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValidatorRegexEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    @AssistedInject ValidatorRegexEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject, @Assisted ValidatorRegex validator) {
        var regex = textField().outlined().label("Regex");
        if(validator!=null) regex.value(validator.pattern());
        container.add(regex);
        regex.onChange(evt-> {
            var next = subject.getValue().toBuilder().validators(List.of(ValidatorRegex.builder().pattern(regex.value()).build())).build();
            subject.next(next);
        });
    }
    @AssistedFactory interface ValidatorRegexEditorElementFactory {
        ValidatorRegexEditorElement regex(BehaviorSubject<AttributeTypeDefinition> subject, ValidatorRegex validator);
    }
}

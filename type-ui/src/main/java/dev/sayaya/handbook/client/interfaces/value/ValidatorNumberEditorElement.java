package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.validator.ValidatorNumber;
import dev.sayaya.handbook.client.domain.validator.ValidatorRegex;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.InputType;
import org.jboss.elemento.IsElement;

import java.util.List;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValidatorNumberEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div().style("""
                    display: flex;
                    flex-direction: row;
                    justify-content: flex-end;
                    gap: 0.5rem;
                """);
    @AssistedInject ValidatorNumberEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject, @Assisted ValidatorNumber validator) {
        var min = textField().outlined().type(InputType.number).label("min").style("max-width: 6rem;");
        var max = textField().outlined().type(InputType.number).label("max").style("max-width: 6rem;");
        if(validator!=null) {
            min.element().valueAsNumber = validator.min();
            max.element().valueAsNumber = validator.max();
        }
        container.add(min).add(max);
        min.onChange(evt-> {
            var next = subject.getValue().toBuilder()
                    .clearValidators()
                    .validators(List.of(ValidatorNumber.builder().min(min.element().valueAsNumber).max(max.element().valueAsNumber).build()))
                    .build();
            subject.next(next);
        });
        max.onChange(evt-> {
            var next = subject.getValue().toBuilder()
                    .clearValidators()
                    .validators(List.of(ValidatorNumber.builder().min(min.element().valueAsNumber).max(max.element().valueAsNumber).build()))
                    .build();
            subject.next(next);
        });
    }
    @AssistedFactory interface ValidatorNumberEditorElementFactory {
        ValidatorNumberEditorElement number(BehaviorSubject<AttributeTypeDefinition> subject, ValidatorNumber validator);
    }
}

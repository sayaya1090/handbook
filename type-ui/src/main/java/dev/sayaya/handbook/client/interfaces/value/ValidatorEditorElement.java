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

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValidatorEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> elem;
    @AssistedInject ValidatorEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject, AttributeEditorElement.AttributeEditorElementFactory factory) {
        var value = subject.getValue()!=null? subject.getValue().baseType().name() : null;
        switch (value) {
            case "Value" -> {
                var regex = textField().outlined().label("Regex");
                elem = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """).add(regex);
                regex.onChange(evt-> {
                    var next = subject.getValue().toBuilder().validators(List.of(ValidatorRegex.builder().pattern(regex.value()).build())).build();
                    subject.next(next);
                });
            }
            case "Array" -> {
                var child = behavior(subject.getValue().arguments().get(0));
                elem = factory.attributeEditorElement(child).style("margin-left: 2rem;");
            }
            case "Map" -> {
                elem = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """).add(factory.attributeEditorElement(behavior(subject.getValue().arguments().get(0))))
                    .add(factory.attributeEditorElement(behavior(subject.getValue().arguments().get(1))));
            }
            case "File" -> {
                elem = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """).add( textField().outlined().label("Extensions"));
            }
            default -> throw new IllegalArgumentException("Unsupported base type: " + value);
        }
    }
    @AssistedFactory
    interface ValidatorEditorElementFactory {
        ValidatorEditorElement validatorEditorElement(BehaviorSubject<AttributeTypeDefinition> def);
    }
}

package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.validator.ValidatorBool;
import dev.sayaya.handbook.client.domain.validator.ValidatorNumber;
import dev.sayaya.handbook.client.domain.validator.ValidatorRegex;
import dev.sayaya.handbook.client.domain.validator.ValidatorSelect;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.InputType;
import org.jboss.elemento.IsElement;

import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValidatorEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> elem;
    @AssistedInject ValidatorEditorElement (
            @Assisted BehaviorSubject<AttributeTypeDefinition> subject,
            AttributeEditorElement.AttributeEditorElementFactory attributeEditorFactory,
            ValidatorEditorFactory validatorEditorFactory
    ) {
        var value = subject.getValue()!=null? subject.getValue().baseType().name() : null;
        switch (value) {
            case "Value" -> {
                elem = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """);
                var validator = subject.getValue().validators().isEmpty() ? null : subject.getValue().validators().get(0);
                if(validator ==null) elem.add(validatorEditorFactory.regex(subject, null));
                else if(validator instanceof ValidatorRegex cast) elem.add(validatorEditorFactory.regex(subject, cast));
                else if(validator instanceof ValidatorBool) {

                } else if(validator instanceof ValidatorNumber) {
                    var min = textField().outlined().type(InputType.number).label("min");
                    var max = textField().outlined().type(InputType.number).label("max");
                    elem.add(min).add(max);
                    min.onChange(evt-> {
                        var next = subject.getValue().toBuilder().validators(List.of(ValidatorNumber.builder().build())).build();
                        subject.next(next);
                    });
                    max.onChange(evt-> {
                        var next = subject.getValue().toBuilder().validators(List.of(ValidatorNumber.builder().build())).build();
                        subject.next(next);
                    });
                } else if(validator instanceof ValidatorSelect cast) elem.add(validatorEditorFactory.select(subject, cast));
                else {
                    DomGlobal.console.error("Unsupported validator type: " + validator.getClass().getName());
                }
            }
            case "Array" -> {
                var child = behavior(subject.getValue().arguments().get(0));
                elem = attributeEditorFactory.attributeEditorElement(child).style("margin-left: 2rem;");
            }
            case "Map" -> {
                elem = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """).add(attributeEditorFactory.attributeEditorElement(behavior(subject.getValue().arguments().get(0))))
                    .add(attributeEditorFactory.attributeEditorElement(behavior(subject.getValue().arguments().get(1))));
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

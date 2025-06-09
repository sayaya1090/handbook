package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.validator.*;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;


import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
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
                elem = container();
                var validator = subject.getValue().validators().isEmpty() ? null : subject.getValue().validators().get(0);
                if(validator ==null) elem.add(validatorEditorFactory.regex(subject, null));
                else if(validator instanceof ValidatorRegex cast) elem.add(validatorEditorFactory.regex(subject, cast));
                else if(validator instanceof ValidatorBool) ;
                else if(validator instanceof ValidatorNumber cast) elem.add(validatorEditorFactory.number(subject, cast));
                else if(validator instanceof ValidatorDate) ;
                else if(validator instanceof ValidatorSelect cast) elem.add(validatorEditorFactory.select(subject, cast));
                else DomGlobal.console.error("Unsupported validator type: " + validator.getClass().getName());
            } case "Array" -> {
                var child = behavior(subject.getValue().arguments().get(0));
                elem = attributeEditorFactory.attributeEditorElement(child).style("margin-left: 2rem;");
                child.subscribe(next->subject.next(subject.getValue().toBuilder().clearArguments().argument(next).build()));
            } case "Map" -> elem = container().add(attributeEditorFactory.attributeEditorElement(behavior(subject.getValue().arguments().get(0))))
                                              .add(attributeEditorFactory.attributeEditorElement(behavior(subject.getValue().arguments().get(1))));
            case "File" -> elem = container().add(validatorEditorFactory.file(subject));
            case "Document" -> elem = container().add(validatorEditorFactory.document(subject));
            default -> throw new IllegalArgumentException("Unsupported base type: " + value);
        }
    }
    private HTMLContainerBuilder<HTMLDivElement> container() {
        return div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """);
    }
    @AssistedFactory
    interface ValidatorEditorElementFactory {
        ValidatorEditorElement validatorEditorElement(BehaviorSubject<AttributeTypeDefinition> def);
    }
}

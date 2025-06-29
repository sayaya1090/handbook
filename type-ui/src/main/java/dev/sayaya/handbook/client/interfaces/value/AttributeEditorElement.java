package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.validator.*;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static org.jboss.elemento.Elements.div;

public class AttributeEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> elem = div();
    private final SelectElementBuilder.OutlinedSelectElementBuilder type = select().outlined().css("type").label("Type");
    private final BehaviorSubject<AttributeTypeDefinition> subject;
    private final ValidatorEditorElement.ValidatorEditorElementFactory factory;
    @AssistedInject AttributeEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject, ValidatorEditorElement.ValidatorEditorElementFactory factory) {
        this.subject = subject;
        this.factory = factory;
        elem.style("""
                border-left: 3px solid var(--md-sys-color-primary);
                padding-left: 0.5rem;
                display: flex;
                flex-direction: column;
                align-items: stretch;
                gap: 0.5rem;
                """).add(type);
        updateTypeOptions();
        updateParamElement();
        type.onChange(evt->setAttributeType());
    }
    private void updateTypeOptions() {
        type.removeAllOptions();
        var types = new String[] {
                "Text", "Number", "Date", "Select", "Boolean", "Array", "Map", "File", "Document"
        };
        String selected = option(subject.getValue());
        for(var t: types) type.option().value(t).headline(t).select(Objects.equals(t, selected));
    }
    private String option(AttributeTypeDefinition attr) {
        if(attr==null) return null;
        if(attr.baseType()==null) return null;
        return switch (attr.baseType()) {
            case Value -> {
                if(attr.validators()==null || attr.validators().isEmpty()) yield "Text";
                else {
                    var validator = attr.validators().get(0);
                    if(validator instanceof ValidatorRegex) yield "Text";
                    else if(validator instanceof ValidatorBool) yield "Boolean";
                    else if(validator instanceof ValidatorNumber) yield "Number";
                    else if(validator instanceof ValidatorSelect) yield "Select";
                    else if(validator instanceof ValidatorDate) yield "Date";
                    else {
                        DomGlobal.console.warn("Unknown validator type: "+validator.getClass().getName());
                        yield "Text";
                    }
                }
            } case Array -> "Array";
            case Map -> "Map";
            case File -> "File";
            case Document -> "Document";
        };
    }
    private void setAttributeType() {
        switch (type.value()) {
            case "Text" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.Value)
                    .validators(List.of(ValidatorRegex.builder().pattern(".*").build()))
                    .build());
            case "Number" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.Value)
                    .validators(List.of(ValidatorNumber.builder().build()))
                    .build());
            case "Date" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.Value)
                    .validators(List.of(ValidatorDate.builder().build()))
                    .build());
            case "Select" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.Value)
                    .validators(List.of(ValidatorSelect.builder().build()))
                    .build());
            case "Boolean" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.Value)
                    .validators(List.of(ValidatorBool.builder().build()))
                    .build());
            case "Array" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.Array)
                    .arguments(List.of(AttributeTypeDefinition.builder().baseType(AttributeTypeDefinition.AttributeType.Value).build()))
                    .build());
            case "Map" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.Map)
                    .arguments(List.of(
                            AttributeTypeDefinition.builder().baseType(AttributeTypeDefinition.AttributeType.Value).build(),
                            AttributeTypeDefinition.builder().baseType(AttributeTypeDefinition.AttributeType.Value).build()))
                    .build());
            case "File" -> subject.next(AttributeTypeDefinition.builder()
                    .baseType(AttributeTypeDefinition.AttributeType.File)
                    .extensions(Set.of())
                    .build());
            case "Document" -> subject.next(subject.getValue().toBuilder()
                    .baseType(AttributeTypeDefinition.AttributeType.Document)
                    .referencedType(null).build());
            default -> {
                DomGlobal.console.warn("Unknown attribute type: "+type.value());
                return;
            }
        }
        updateParamElement();
    }
    private IsElement<?> param = null;
    private void updateParamElement() {
        if (param != null) param.element().remove();
        var value = subject.getValue()!=null? subject.getValue().baseType().name() : null;
        if(value!=null) {
            param = factory.validatorEditorElement(subject);
            elem.add(param);
        }
    }
    @AssistedFactory
    interface AttributeEditorElementFactory {
        AttributeEditorElement attributeEditorElement(BehaviorSubject<AttributeTypeDefinition> subject);
    }
}

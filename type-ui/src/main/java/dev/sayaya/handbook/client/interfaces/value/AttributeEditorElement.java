package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.LayoutTypeList;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static org.jboss.elemento.Elements.div;

public class AttributeEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> elem = div();
    private final SelectElementBuilder.OutlinedSelectElementBuilder type = select().outlined().css("type").label("Type");
    private final SelectElementBuilder.OutlinedSelectElementBuilder reference = select().outlined().css("type").label("Reference Type").style("display: none;");
    private final BehaviorSubject<AttributeTypeDefinition> subject;
    private final ValidatorEditorElement.ValidatorEditorElementFactory factory;
    @AssistedInject AttributeEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject, LayoutTypeList typeListEditing, ValidatorEditorElement.ValidatorEditorElementFactory factory) {
        this.subject = subject;
        this.factory = factory;
        elem.style("""
                border-left: 3px solid var(--md-sys-color-primary);
                padding-left: 0.5rem;
                display: flex;
                flex-direction: column;
                align-items: stretch;
                gap: 0.5rem;
                """).add(type)
                .add(reference);
        updateTypeOptions(AttributeTypeDefinition.AttributeType.values());
        updateParamElement();
        typeListEditing.distinctUntilChanged().subscribe(this::updateReferenceOptions);
        type.onChange(evt->setAttributeType());
        reference.onChange(evt->setReferenceType());
    }
    private void updateTypeOptions(AttributeTypeDefinition.AttributeType[] types) {
        type.removeAllOptions();
        for(var t: types) type.option()
                .value(t.name()).headline(t.name())
                .select(subject.getValue()!=null && subject.getValue().baseType().equals(t));
    }
    private void updateReferenceOptions(Set<Type> types) {
        reference.removeAllOptions();
        types.stream().map(Type::name).sorted().forEach(t->reference.option().value(t).headline(t));
    }
    private void setAttributeType() {
        var baseType = AttributeTypeDefinition.AttributeType.valueOf(type.value());
        var def = subject.getValue().toBuilder().baseType(baseType);
        switch (baseType) {
            case Array ->{
                if(subject.getValue().arguments().isEmpty()) def.arguments(List.of(AttributeTypeDefinition.builder().baseType(AttributeTypeDefinition.AttributeType.Value).build()));
            } case Map -> {
                if(subject.getValue().arguments().size() < 2) {
                    var tmp = new LinkedList<>(subject.getValue().arguments());
                    while(tmp.size() < 2) tmp.add(AttributeTypeDefinition.builder().baseType(AttributeTypeDefinition.AttributeType.Value).build());
                    def.clearArguments().arguments(tmp);
                }
            } case File -> {
                if(subject.getValue().extensions() == null) def.extensions(Set.of());
            }
        }
        subject.next(def.build());
        updateParamElement();
    }
    private IsElement<?> param = null;
    private void updateParamElement() {
        if (param != null) param.element().remove();
        var value = subject.getValue()!=null? subject.getValue().baseType().name() : null;
        if(value!=null) switch (value) {
            case "Value", "Array", "Map", "File" -> {
                param = factory.validatorEditorElement(subject);
                elem.add(param);
                reference.element().style.display = "none";
            }
            case "Document" -> reference.element().style.display = null;
        }
    }
    private void setReferenceType() {
        var def = subject.getValue();
        def.referencedType(reference.value());
        subject.next(def);
    }
    @AssistedFactory
    interface AttributeEditorElementFactory {
        AttributeEditorElement attributeEditorElement(BehaviorSubject<AttributeTypeDefinition> subject);
    }
}

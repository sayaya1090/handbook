package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.LayoutTypeList;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class AttributeEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> elem = div();
    private final SelectElementBuilder.OutlinedSelectElementBuilder type = select().outlined().css("type").label("Type");
    private final SelectElementBuilder.OutlinedSelectElementBuilder reference = select().outlined().css("type").label("Reference Type").style("display: none;");
    private final LayoutTypeList typeListEditing;
    private IsElement<HTMLDivElement> child = null;
    private AttributeTypeDefinition def;
    @AssistedInject AttributeEditorElement(@Assisted AttributeTypeDefinition def, LayoutTypeList typeListEditing) {
        this.def = def;
        this.typeListEditing = typeListEditing;
        elem.style("""
                border-left: 3px solid var(--md-sys-color-primary);
                padding-left: 0.5rem;
                display: flex;
                flex-direction: column;
                align-items: stretch;
                gap: 0.5rem;
                """).add(type)
                .add(reference);
        updateTypes(AttributeTypeDefinition.AttributeType.values());
        typeListEditing.distinctUntilChanged().subscribe(this::updateReferences);
        type.onChange(evt->{
            this.def.baseType(AttributeTypeDefinition.AttributeType.valueOf(type.value()));
            switch (this.def.baseType()) {
                case Array ->{
                    if(def.arguments().isEmpty()) def.arguments(List.of(AttributeTypeDefinition.builder().baseType(AttributeTypeDefinition.AttributeType.Value).build()));
                } case Map -> {
                    var args = def.arguments();
                    if(def.arguments().size() < 2) {
                        var tmp = new LinkedList<>(def.arguments());
                        while(tmp.size() < 2) tmp.add(AttributeTypeDefinition.builder().baseType(AttributeTypeDefinition.AttributeType.Value).build());
                        def.arguments(tmp);
                    }
                } case File -> {
                    if(def.extensions() == null) def.extensions(Set.of());
                }
            }
            updateParam();
        });
    }
    private void updateTypes(AttributeTypeDefinition.AttributeType[] types) {
        type.removeAllOptions();
        for(var t: types) type.option()
                .value(t.name()).headline(t.name())
                .select(def.baseType().equals(t));
        updateParam();
    }
    private void updateReferences(Set<Type> types) {
        reference.removeAllOptions();
        types.stream().map(Type::id).sorted().forEach(t->reference.option().value(t).headline(t));
    }
    private void updateParam() {
        var value = def.baseType().name();
        switch (value) {
            case "Value" -> {
                if (child != null) child.element().remove();
                child = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """).add( textField().outlined().label("Regex"));
                elem.add(child);
                reference.element().style.display = "none";
            }
            case "Array" -> {
                if (child != null) child.element().remove();
                child = new AttributeEditorElement(def.arguments().get(0), typeListEditing).style("margin-left: 2rem;");
                elem.add(child);
                reference.element().style.display = "none";
            }
            case "Map" -> {
                if (child != null) child.element().remove();
                child = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """).add(new AttributeEditorElement(def.arguments().get(0), typeListEditing))
                        .add(new AttributeEditorElement(def.arguments().get(1), typeListEditing));
                elem.add(child);
                reference.element().style.display = "none";
            }
            case "File" -> {
                if (child != null) child.element().remove();
                child = div().style("""
                    display: flex;
                    flex-direction: column;
                    align-items: stretch;
                    gap: 0.5rem;
                    margin-left: 2rem;
                """).add( textField().outlined().label("Extensions"));
                elem.add(child);
                reference.element().style.display = "none";
            }
            case "Document" -> {
                if (child != null) child.element().remove();
                reference.element().style.display = null;
            }
        }
    }
    @AssistedFactory
    interface AttributeEditorElementFactory {
        AttributeEditorElement attributeEditorElement(AttributeTypeDefinition def);
    }
}

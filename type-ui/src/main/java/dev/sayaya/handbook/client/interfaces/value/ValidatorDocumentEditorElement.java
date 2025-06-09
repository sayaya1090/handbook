package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.LayoutTypeList;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import java.util.Set;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static org.jboss.elemento.Elements.div;

public class ValidatorDocumentEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    private final SelectElementBuilder.OutlinedSelectElementBuilder reference;
    private final BehaviorSubject<AttributeTypeDefinition> subject;
    @AssistedInject ValidatorDocumentEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject, LayoutTypeList typeListEditing) {
        this.subject = subject;
        reference = select().outlined().css("type").label("Reference Type");
        container.add(reference);
        typeListEditing.distinctUntilChanged().subscribe(this::updateReferenceOptions);
        reference.onChange(evt-> {
            var next = subject.getValue().toBuilder().referencedType(reference.value()).build();
            subject.next(next);
        });
    }
    private void updateReferenceOptions(Set<Type> types) {
        reference.removeAllOptions();
        var prev = subject.getValue().referencedType();
        types.stream().map(Type::name).sorted().forEach(t-> reference.option().value(t).headline(t).select(t.equals(prev)));
    }
    @AssistedFactory interface ValidatorRegexEditorElementFactory {
        ValidatorDocumentEditorElement document(BehaviorSubject<AttributeTypeDefinition> subject);
    }
}

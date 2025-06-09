package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.validator.ValidatorRegex;
import dev.sayaya.handbook.client.usecase.LayoutTypeList;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValidatorFileEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container = div();
    @AssistedInject ValidatorFileEditorElement(@Assisted BehaviorSubject<AttributeTypeDefinition> subject) {
        var extensions = textField().outlined().label("Extensions").supportingText("',' separated");
        extensions.value(subject.getValue().extensions().stream().sorted().collect(Collectors.joining(", ")));
        container.add(extensions);
        extensions.onChange(evt-> {
            Set<String> set = null;
            if(extensions.value()!=null) set = Arrays.stream(extensions.value().split(",")).map(String::trim).collect(Collectors.toSet());
            var next = subject.getValue().toBuilder().clearExtensions().extensions(set).build();
            subject.next(next);
        });
    }
    @AssistedFactory interface ValidatorFileEditorElementFactory {
        ValidatorFileEditorElement file(BehaviorSubject<AttributeTypeDefinition> subject);
    }
}

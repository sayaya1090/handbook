package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

public class ValidatorEditorElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> elem = div();

    @AssistedInject ValidatorEditorElement(@Assisted AttributeTypeDefinition def) {

    }
    @AssistedFactory
    interface ValidatorEditorElementFactory {
        ValidatorEditorElement validatorEditorElement(AttributeTypeDefinition def);
    }
}

package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.Event;
import jsinterop.base.Js;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

public class TypeNameElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined().css("label");
    private final BoxElement parent;
    private final ActionManager actionManager;
    @AssistedInject TypeNameElement(@Assisted BoxElement parent, ActionManager actionManager) {
        this.parent = parent;
        this.actionManager = actionManager;
        ipt.on(EventType.change, this::update);
    }
    private void update(Event evt) {
        var ipt = Js.asPropertyMap(evt.target);
        actionManager.title(parent, ipt.get("value").toString());
    }
    void update(Type type) {
        ipt.value(type.id());
    }
    @AssistedFactory
    interface TypeNameElementFactory {
        TypeNameElement create(BoxElement parent);
    }
}

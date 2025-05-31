package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import jsinterop.base.Js;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

public class TypeNameElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined()
            .css("label").required().placeholder("Type Name");
    private final TypeElement parent;
    private final ActionManager actionManager;
    @AssistedInject TypeNameElement(@Assisted TypeElement parent, ActionManager actionManager) {
        this.parent = parent;
        this.actionManager = actionManager;
        ipt.on(EventType.change, this::update);
        parent.subscribe(evt->update(parent.value()));
    }
    private void update(Event evt) {
        var ipt = Js.asPropertyMap(evt.target);
        var next = parent.value().toBuilder().name(ipt.get("value").toString()).build();
        actionManager.edit(parent, next);
    }
    private void update(Type type) {
        ipt.value(type.name());
    }
    @AssistedFactory
    interface TypeNameElementFactory {
        TypeNameElement create(TypeElement parent);
    }
}

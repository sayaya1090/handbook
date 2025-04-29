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

public class TypeVersionElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined().style("""
                --_outline-width: 0;
                --_leading-space: 5px;
                --_trailing-space: 5px;
                --_top-space: 5px;
                --_bottom-space: 5px;
            """).label("Version");
    private final BoxElement parent;
    private final ActionManager actionManager;
    @AssistedInject TypeVersionElement(@Assisted BoxElement parent, ActionManager actionManager) {
        this.parent = parent;
        this.actionManager = actionManager;
        ipt.on(EventType.change, this::update);
    }
    private void update(Event evt) {
        var ipt = Js.asPropertyMap(evt.target);
        actionManager.version(parent, ipt.get("value").toString());
    }
    void update(Type type) {
        ipt.value(type.version());
    }
    @AssistedFactory
    interface TypeVersionElementFactory {
        TypeVersionElement create(BoxElement parent);
    }
}

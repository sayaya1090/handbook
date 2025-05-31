package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import java.util.function.Consumer;
import java.util.function.Function;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

class TypeVersionElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt;
    private final TypeElement parent;
    private final ActionManager actionManager;
    @AssistedInject TypeVersionElement(@Assisted TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt, @Assisted TypeElement parent, ActionManager actionManager) {
        this.ipt = ipt.style("""
                --_outline-width: 0;
                --_leading-space: 5px;
                --_trailing-space: 5px;
                --_top-space: 5px;
                --_bottom-space: 5px;
                direction: rtl;
                text-align: right;
                text-direction: ltr;
            """).label("Version");
        ipt.on(EventType.change, evt->version(ipt.value()));
        this.parent = parent;
        this.actionManager = actionManager;
        parent.subscribe(evt->update(parent.value()));
    }
    private void version(String value) {
        var next = parent.value().toBuilder().version(value).build();
        actionManager.edit(parent, next);
    }
    private void update(Type type) {
        ipt.value(type.version()!=null? type.version() : "");
    }
    @AssistedFactory
    interface TypeVersionElementFactory {
        TypeVersionElement create(TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt, TypeElement parent);
        default TypeVersionElement create(TypeElement parent) {
            return create(textField().outlined(), parent);
        }
    }
}

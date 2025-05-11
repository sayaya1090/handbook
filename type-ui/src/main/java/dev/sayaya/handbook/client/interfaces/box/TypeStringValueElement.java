package dev.sayaya.handbook.client.interfaces.box;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import java.util.function.Consumer;
import java.util.function.Function;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

public class TypeStringValueElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt;
    private final Function<Type, String> toValue;
    @AssistedInject TypeStringValueElement(@Assisted TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt,
                           @Assisted Consumer<String> callback, @Assisted Function<Type, String> toValue) {
        this.ipt = ipt.style("""
                --_outline-width: 0;
                --_leading-space: 5px;
                --_trailing-space: 5px;
                --_top-space: 5px;
                --_bottom-space: 5px;
            """);
        this.toValue = toValue;
        ipt.on(EventType.change, evt->callback.accept(ipt.value()));
    }
    TypeStringValueElement alignRight() {
        style("direction: rtl; text-align: right;");
        element().textDirection = "ltr";
        return this;
    }
    void update(Type type) {
        ipt.value(toValue.apply(type) == null ? "" : toValue.apply(type));
    }
    @AssistedFactory
    interface TypeValueElementFactory {
        TypeStringValueElement create(TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt, Consumer<String> callback, Function<Type, String> toValue);
        default TypeStringValueElement create(String label, Consumer<String> callback, Function<Type, String> toValue) {
            return create(textField().outlined().label(label), callback, toValue);
        }
    }
}

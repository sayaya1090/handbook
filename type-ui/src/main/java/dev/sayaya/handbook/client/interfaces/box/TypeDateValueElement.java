package dev.sayaya.handbook.client.interfaces.box;

import com.google.gwt.core.client.JsDate;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

public class TypeDateValueElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt;
    private final Function<Type, Date> toValue;
    @AssistedInject TypeDateValueElement(@Assisted TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt,
                                     @Assisted Consumer<Date> callback, @Assisted Function<Type, Date> toValue) {
        this.ipt = ipt.style("""
                --_outline-width: 0;
                --_leading-space: 5px;
                --_trailing-space: 5px;
                --_top-space: 5px;
                --_bottom-space: 5px;
            """);
        this.toValue = toValue;
        ipt.on(EventType.change, evt->{
            JsDate cast = JsDate.create(ipt.element().valueAsNumber.longValue());
            var date = fromLocalDatetimeToUtc(cast);
            callback.accept(new Date(date));
        });
    }
    void update(Type type) {
        var date = toValue.apply(type);
        if(date==null) ipt.element().valueAsNumber = null;
        else {
            JsDate cast = JsDate.create(date.getTime());
            ipt.element().valueAsNumber = (fromUtcToLocalDatetime(cast) / 1000) * 1000.0;
        }
    }
    private static long fromUtcToLocalDatetime(JsDate date) {
        var offset = date.getTimezoneOffset();
        return (long) (date.getTime() - offset*60*1000);
    }
    private static long fromLocalDatetimeToUtc(JsDate date) {
        var offset = date.getTimezoneOffset();
        return (long)(date.getTime() + offset*60*1000);
    }
    @AssistedFactory
    interface TypeDateValueElementFactory {
        TypeDateValueElement create(TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt, Consumer<Date> callback, Function<Type, Date> toValue);
        default TypeDateValueElement create(String label, Consumer<Date> callback, Function<Type, Date> toValue) {
            return create(textField().outlined().attr("type", "datetime-local").label(label), callback, toValue);
        }
    }
}

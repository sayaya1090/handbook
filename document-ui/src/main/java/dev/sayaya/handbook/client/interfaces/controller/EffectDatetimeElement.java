package dev.sayaya.handbook.client.interfaces.controller;

import com.google.gwt.core.client.JsDate;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.Event;
import jsinterop.base.Js;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

@Singleton
public class EffectDatetimeElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined().attr("type", "datetime-local")
            .css("label")
            .label("Effect at").enable(false);
    @Inject
    EffectDatetimeElement() {
        ipt.on(EventType.change, this::update);
        //ipt.element().disabled = true;
        ipt.element().readOnly = true;
    }
    private void update(Event evt) {
        var ipt = Js.asPropertyMap(evt.target);
    }

    void update(Type type) {
        /*var date = toValue.apply(type);
        if(date==null) ipt.element().valueAsNumber = null;
        else {
            JsDate cast = JsDate.create(date.getTime());
            ipt.element().valueAsNumber = (fromUtcToLocalDatetime(cast) / 1000) * 1000.0;
        }*/
    }
    private static long fromUtcToLocalDatetime(JsDate date) {
        var offset = date.getTimezoneOffset();
        return (long) (date.getTime() - offset*60*1000);
    }
    private static long fromLocalDatetimeToUtc(JsDate date) {
        var offset = date.getTimezoneOffset();
        return (long)(date.getTime() + offset*60*1000);
    }
}

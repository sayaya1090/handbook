package dev.sayaya.handbook.client.interfaces.controller;

import com.google.gwt.core.client.JsDate;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.Event;
import jsinterop.base.Js;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.Label.findLabelOrDefault;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

@Singleton
public class ExpireDatetimeElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder ipt = textField().outlined().attr("type", "datetime-local")
            .css("label").style("width: auto;").enable(false);
    @Inject ExpireDatetimeElement(TypeProvider typeProvider, Observable<Label> labels) {
        labels.subscribe(this::update);
        ipt.element().readOnly = true;
        typeProvider.subscribe(this::update);
    }
    void update(Type type) {
        var date = type!=null ? type.expireDateTime() : null;
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
    private void update(Label label) {
        ipt.label(findLabelOrDefault(label, "Expire date"));
    }
}

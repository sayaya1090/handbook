package dev.sayaya.handbook.client.interfaces.controller;

import com.google.gwt.core.client.JsDate;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.usecase.BasetimeProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdTextFieldElement;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

import static dev.sayaya.handbook.client.domain.Label.findLabelOrDefault;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;

@Singleton
class DateTimeSelectorElement implements IsElement<MdTextFieldElement.MdOutlinedTextFieldElement> {
    @Delegate private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder field = textField().outlined()
            .attr("type", "datetime-local")
            .style("""
                width: 15rem;
                text-align: center;
                --md-outlined-field-top-space: 6px;
                --md-outlined-field-bottom-space: 6px;
            """);
    @Inject DateTimeSelectorElement(BasetimeProvider provider, Observable<Label> labels) {
        provider.distinctUntilChanged().subscribe(this::update);
        labels.distinctUntilChanged().subscribe(this::update);
        field.onChange(evt->{
            JsDate cast = JsDate.create(field.element().valueAsNumber.longValue());
            var date = fromLocalDatetimeToUtc(cast);
            provider.next(new Date(date));
        });
    }
    private void update(Date date) {
        JsDate cast = JsDate.create(date.getTime());
        field.element().valueAsNumber = (fromUtcToLocalDatetime(cast) / 1000) * 1000.0;
    }
    private void update(Label label) {
        field.label(findLabelOrDefault(label, "Base Datetime"));
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

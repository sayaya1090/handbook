package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.handbook.client.usecase.AttributeTypeList;
import dev.sayaya.rx.subject.Subject;
import dev.sayaya.ui.elements.SelectElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValueElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueElement(@Assisted Subject<Value> value, AttributeTypeList typeList) {
        this(div(), value, typeList);
    }
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title = textField().outlined().css("label");
    private final SelectElementBuilder.OutlinedSelectElementBuilder type = select().outlined().css("type");
    private ValueElement(HTMLContainerBuilder<HTMLDivElement> element, Subject<Value> value, AttributeTypeList typeList) {
        super(element.element());
        value.subscribe(this::update);
        typeList.subscribe(this::update);
        element.css("property").add(title).add(type);
        title.onChange(evt->target.name(title.value()));
        type.onChange(evt->target.type(type.element().value));
    }
    private Value target;
    private void update(Value value) {
        this.target = value;
        title.value(value.name());
        type.element().value = value.type();
    }
    private void update(String[] types) {
        type.removeAllOptions();
        for(var t: types) type.option().value(t).headline(t);
    }
    @AssistedFactory
    interface ValueElementFactory {
        ValueElement valueElement(Subject<Value> value);
    }
}

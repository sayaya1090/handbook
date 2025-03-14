package dev.sayaya.handbook.client.domain.value;

import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.rx.subject.Subject;
import dev.sayaya.ui.elements.SelectElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;

public class ValueElement extends HTMLContainerBuilder<HTMLDivElement> {
    public ValueElement(Subject<Value> value) {
        this(div(), value);
    }
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder title = textField().outlined().css("label");
    private final SelectElementBuilder.OutlinedSelectElementBuilder type = select().outlined().css("type");
    private ValueElement(HTMLContainerBuilder<HTMLDivElement> element, Subject<Value> value) {
        super(element.element());
        value.subscribe(this::update);
        element.css("property").add(title).add(type);
        type.option().value("c").headline("Color").end()
                .option().value("d").headline("Color2").end()
                .option().value("e").headline("Color3").end();
        title.onChange(evt->target.name(title.value()));
        type.onChange(evt->target.type(type.element().value));
    }
    private Value target;
    private void update(Value value) {
        this.target = value;
        title.value(value.name());
        type.element().value = value.type();
    }
}

package dev.sayaya.handbook.client.domain.value;

import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.List;

import static dev.sayaya.rx.subject.Subject.subject;
import static org.jboss.elemento.Elements.div;

public class ValueListElement extends HTMLContainerBuilder<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container;
    public ValueListElement(Subject<List<Value>> values) {
        this(div(), values);
    }
    private ValueListElement(HTMLContainerBuilder<HTMLDivElement> div, Subject<List<Value>> values) {
        super(div.element());
        this.container = div;
        container.style("""
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                align-items: stretch;
                padding: 0 1rem;
                """);
        values.subscribe(this::update);
    }
    private void update(List<Value> values) {
        container.element().innerHTML = "";
        for(var value: values) {
            Subject<Value> valueSubject = subject(Value.class);
            var elem = new ValueElement(valueSubject);
            valueSubject.next(value);
            container.add(elem);
        }
    }
}

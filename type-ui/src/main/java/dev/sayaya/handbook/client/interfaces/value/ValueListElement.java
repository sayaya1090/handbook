package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.List;

import static dev.sayaya.rx.subject.Subject.subject;
import static org.jboss.elemento.Elements.div;

public class ValueListElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueListElement(@Assisted Subject<List<Attribute>> values, ValueElement.ValueElementFactory elementFactory) {
        this(div(), values, elementFactory);
    }
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container;
    private final ValueElement.ValueElementFactory elementFactory;
    private ValueListElement(HTMLContainerBuilder<HTMLDivElement> div, Subject<List<Attribute>> values, ValueElement.ValueElementFactory elementFactory) {
        super(div.element());
        this.container = div;
        this.elementFactory = elementFactory;
        container.css("properties");
        values.subscribe(this::update);
    }
    private void update(List<Attribute> values) {
        container.element().innerHTML = "";
        for(var value: values) {
            Subject<Attribute> valueSubject = subject(Attribute.class);
            var elem = elementFactory.valueElement(valueSubject);
            valueSubject.next(value);
            container.add(elem);
        }
    }
    @AssistedFactory
    public interface ValueListElementFactory {
        ValueListElement valueList(Subject<List<Attribute>> values);
    }
}

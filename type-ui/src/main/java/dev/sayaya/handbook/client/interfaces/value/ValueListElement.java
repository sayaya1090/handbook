package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.rx.subject.Subject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.List;
import java.util.Map;

import static org.jboss.elemento.Elements.div;

public class ValueListElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueListElement(@Assisted Subject<List<Attribute>> values, @Assisted TypeElement parent, ValueElement.ValueElementFactory elementFactory) {
        this(div(), values, parent, elementFactory);
    }
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container;
    private final Map<Attribute, ValueElement> elements = new java.util.HashMap<>();
    private final ValueElement.ValueElementFactory elementFactory;
    private final TypeElement parent;
    private ValueListElement(HTMLContainerBuilder<HTMLDivElement> div, Subject<List<Attribute>> values, TypeElement parent, ValueElement.ValueElementFactory elementFactory) {
        super(div.element());
        this.container = div;
        this.elementFactory = elementFactory;
        this.parent = parent;
        container.css("properties");
        values.subscribe(this::update);
    }
    private void update(List<Attribute> values) {
        container.element().innerHTML = "";
        for(var value: values) {
            ValueElement elem = elements.computeIfAbsent(value, v->elementFactory.valueElement(v, parent));
            container.add(elem);
            elem.update(value);
        }
    }
    @AssistedFactory
    public interface ValueListElementFactory {
        ValueListElement valueList(Subject<List<Attribute>> values, TypeElement parent);
    }
}

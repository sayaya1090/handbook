package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.Map;

import static org.jboss.elemento.Elements.div;

public class ValueListElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueListElement(@Assisted BehaviorSubject<TypeElement> parent, ValueElement.ValueElementFactory elementFactory) {
        this(div(), parent, elementFactory);
    }
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container;
    private final Map<Attribute, ValueElement> elements = new java.util.HashMap<>();
    private final ValueElement.ValueElementFactory elementFactory;
    private ValueListElement(HTMLContainerBuilder<HTMLDivElement> div, BehaviorSubject<TypeElement> parent, ValueElement.ValueElementFactory elementFactory) {
        super(div.element());
        this.container = div;
        this.elementFactory = elementFactory;
        container.css("properties");
        parent.subscribe(this::update);
    }
    private void update(TypeElement parent) {
        var values = parent.value().attributes();
        container.element().innerHTML = "";
        for(var value: values) {
            ValueElement elem = elements.computeIfAbsent(value, v->elementFactory.valueElement(v, parent));
            container.add(elem);
            elem.update(value);
        }
    }
    @AssistedFactory
    public interface ValueListElementFactory {
        ValueListElement valueList(BehaviorSubject<TypeElement> parent);
    }
}

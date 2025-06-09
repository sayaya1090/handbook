package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.LinkedList;
import java.util.List;

import static org.jboss.elemento.Elements.div;

public class ValueListElement extends HTMLContainerBuilder<HTMLDivElement> {
    @AssistedInject ValueListElement(@Assisted BehaviorSubject<TypeElement> parent, ValueElement.ValueElementFactory elementFactory) {
        this(div(), parent, elementFactory);
    }
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> container;
    private final ValueElement.ValueElementFactory elementFactory;
    private final List<ValueElement> values = new LinkedList<>();
    private ValueListElement(HTMLContainerBuilder<HTMLDivElement> div, BehaviorSubject<TypeElement> parent, ValueElement.ValueElementFactory elementFactory) {
        super(div.element());
        this.container = div;
        this.elementFactory = elementFactory;
        container.css("properties");
        // 타입이 변경되면 업데이트 호출
        parent.map(TypeElement::value).distinctUntilChanged().subscribe(type->update(parent.getValue(), type));
    }
    private void update(TypeElement parent, Type type) {
        for(var child: this.values) child.clear();
        this.values.clear();
        var values = type.attributes();
        container.element().innerHTML = "";
        for(var value: values) {
            var elem = elementFactory.valueElement(value, parent);
            container.add(elem);
            this.values.add(elem);
            elem.update();
        }
    }
    @AssistedFactory
    public interface ValueListElementFactory {
        ValueListElement valueList(BehaviorSubject<TypeElement> parent);
    }
}

package dev.sayaya.handbook.client.interfaces.value;

import dev.sayaya.handbook.client.domain.AttributeValue;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/** 타입 카드 안의 속성 목록. */
public class ValueListElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final List<ValueElement> elements = new ArrayList<>();
    private Consumer<AttributeValue> onEdit;
    private Consumer<AttributeValue> onDelete;

    public ValueListElement() {
        root = div().css("type-attr-list").element();
    }

    public void setOnEdit(Consumer<AttributeValue> onEdit) { this.onEdit = onEdit; }
    public void setOnDelete(Consumer<AttributeValue> onDelete) { this.onDelete = onDelete; }

    public void update(AttributeValue[] attributes) {
        root.innerHTML = "";
        elements.clear();
        if (attributes == null) return;
        for (AttributeValue attr : attributes) {
            ValueElement elem = new ValueElement(attr, onEdit, onDelete);
            elements.add(elem);
            root.appendChild(elem.element());
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}

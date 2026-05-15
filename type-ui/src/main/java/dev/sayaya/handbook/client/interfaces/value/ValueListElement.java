package dev.sayaya.handbook.client.interfaces.value;

import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.domain.Attribute;
import elemental2.dom.DomGlobal;
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
    private Consumer<Attribute> onEdit;
    private Consumer<Attribute> onDelete;

    public ValueListElement() {
        root = div().css("type-attr-list").element();
    }

    public void setOnEdit(Consumer<Attribute> onEdit) { this.onEdit = onEdit; }
    public void setOnDelete(Consumer<Attribute> onDelete) { this.onDelete = onDelete; }

    public void update(String typeKey, Attribute[] attributes, ChangeTracker tracker) {
        root.innerHTML = "";
        elements.clear();
        if (attributes == null) return;
        for (Attribute attr : attributes) {
            ValueElement elem = new ValueElement(typeKey, attr, onEdit, onDelete, tracker);
            elements.add(elem);
            root.appendChild(elem.element());
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}

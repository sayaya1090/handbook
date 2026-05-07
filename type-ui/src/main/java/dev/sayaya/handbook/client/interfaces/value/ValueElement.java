package dev.sayaya.handbook.client.interfaces.value;

import dev.sayaya.handbook.domain.Attribute;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/** 타입 카드 안의 속성 한 줄. 이름 + 타입 표시 + 삭제 버튼. */
public class ValueElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final Attribute attribute;

    public ValueElement(Attribute attribute, Consumer<Attribute> onEdit, Consumer<Attribute> onDelete) {
        this.attribute = attribute;
        HTMLDivElement nameDiv = div().css("type-attr-name").element();
        nameDiv.textContent = attribute.name();
        HTMLDivElement typeDiv = div().css("type-attr-type").element();
        typeDiv.textContent = attribute.type() != null ? attribute.type().simplify() : "text";

        HTMLElement deleteBtn = (HTMLElement) DomGlobal.document.createElement("span");
        deleteBtn.classList.add("type-attr-delete");
        deleteBtn.textContent = "\u00D7"; // ×
        deleteBtn.addEventListener("click", e -> {
            e.stopPropagation();
            if (onDelete != null) onDelete.accept(attribute);
        });
        root = div().css("type-attr-row").element();
        root.appendChild(nameDiv);
        root.appendChild(typeDiv);
        root.appendChild(deleteBtn);
        root.addEventListener("click", e -> {
            e.stopPropagation();
            if (onEdit != null) onEdit.accept(attribute);
        });
    }

    public Attribute getAttribute() { return attribute; }

    @Override
    public HTMLDivElement element() { return root; }
}

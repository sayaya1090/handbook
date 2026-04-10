package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/** Document 타입: 참조할 타입 이름 입력. 현재 TypeList에서 자동완성 후보를 제공할 수 있다. */
public class DocumentValidatorEditor implements ValidatorEditor {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder refField;

    public DocumentValidatorEditor(TypeList typeList) {
        refField = TextFieldElementBuilder.textField().outlined().label("Referenced type");
        root = div().css("validator-editor")
                .add(refField)
                .element();
    }

    @Override
    public void load(AttributeTypeValue value) {
        refField.value(value != null && value.referencedType != null ? value.referencedType : "");
    }

    @Override
    public AttributeTypeValue collect() {
        String ref = refField.value();
        return AttributeTypeValue.document(ref != null && !ref.trim().isEmpty() ? ref.trim() : null);
    }

    @Override
    public HTMLElement element() { return root; }
}

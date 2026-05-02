package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.domain.AttributeType;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/** Enum 타입: 허용 값 목록 입력 (한 줄에 하나). */
public class EnumValidatorEditor implements ValidatorEditor {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder valuesField;

    public EnumValidatorEditor() {
        valuesField = TextFieldElementBuilder.textField().outlined().label("Allowed values (one per line)");
        root = div().css("validator-editor")
                .add(valuesField)
                .element();
    }

    @Override
    public void load(AttributeType value) {
        if (value != null && value.allowedValues != null) {
            valuesField.value(String.join("\n", value.allowedValues));
        } else {
            valuesField.value("");
        }
    }

    @Override
    public AttributeType collect() {
        String raw = valuesField.value();
        String[] values = (raw == null || raw.trim().isEmpty()) ? null : raw.split("\n");
        return AttributeType.enumType(values);
    }

    @Override
    public HTMLElement element() { return root; }
}

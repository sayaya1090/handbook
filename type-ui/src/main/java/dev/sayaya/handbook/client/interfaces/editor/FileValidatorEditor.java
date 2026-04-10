package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/** File 타입: 허용 확장자 목록 입력 (한 줄에 하나). */
public class FileValidatorEditor implements ValidatorEditor {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder extField;

    public FileValidatorEditor() {
        extField = TextFieldElementBuilder.textField().outlined().label("Allowed extensions (one per line)");
        root = div().css("validator-editor")
                .add(extField)
                .element();
    }

    @Override
    public void load(AttributeTypeValue value) {
        if (value != null && value.extensions != null) {
            extField.value(String.join("\n", value.extensions));
        } else {
            extField.value("");
        }
    }

    @Override
    public AttributeTypeValue collect() {
        String raw = extField.value();
        String[] exts = (raw == null || raw.trim().isEmpty()) ? null : raw.split("\n");
        return AttributeTypeValue.file(exts);
    }

    @Override
    public HTMLElement element() { return root; }
}

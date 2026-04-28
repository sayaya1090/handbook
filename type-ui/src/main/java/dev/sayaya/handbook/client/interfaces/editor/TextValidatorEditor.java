package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.domain.AttributeTypeValue;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/** Text 타입: regex 패턴 입력. 여러 줄(줄바꿈 구분)로 복수 패턴 지원. */
public class TextValidatorEditor implements ValidatorEditor {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder regexField;

    public TextValidatorEditor() {
        regexField = TextFieldElementBuilder.textField().outlined().label("Regex patterns (one per line)");
        root = div().css("validator-editor")
                .add(regexField)
                .element();
    }

    @Override
    public void load(AttributeTypeValue value) {
        if (value != null && value.regexPatterns != null) {
            regexField.value(String.join("\n", value.regexPatterns));
        } else {
            regexField.value("");
        }
    }

    @Override
    public AttributeTypeValue collect() {
        String raw = regexField.value();
        String[] patterns = (raw == null || raw.trim().isEmpty()) ? null : raw.split("\n");
        AttributeTypeValue v = AttributeTypeValue.text();
        v.regexPatterns = patterns;
        return v;
    }

    @Override
    public HTMLElement element() { return root; }
}

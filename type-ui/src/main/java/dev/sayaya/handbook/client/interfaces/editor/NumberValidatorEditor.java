package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.domain.AttributeTypeValue;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/** Number 타입: min/max 범위 입력. */
public class NumberValidatorEditor implements ValidatorEditor {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder minField;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder maxField;

    public NumberValidatorEditor() {
        minField = TextFieldElementBuilder.textField().outlined().label("Min");
        maxField = TextFieldElementBuilder.textField().outlined().label("Max");
        root = div().css("validator-editor", "validator-editor-row")
                .add(minField)
                .add(maxField)
                .element();
    }

    @Override
    public void load(AttributeTypeValue value) {
        minField.value(value != null && value.min != null ? String.valueOf(value.min) : "");
        maxField.value(value != null && value.max != null ? String.valueOf(value.max) : "");
    }

    @Override
    public AttributeTypeValue collect() {
        Double min = parseDouble(minField.value());
        Double max = parseDouble(maxField.value());
        return AttributeTypeValue.number(min, max);
    }

    private static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    @Override
    public HTMLElement element() { return root; }
}

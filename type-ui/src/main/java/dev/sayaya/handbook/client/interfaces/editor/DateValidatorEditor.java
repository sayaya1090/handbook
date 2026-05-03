package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.domain.AttributeType;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/** Date 타입: after/before 범위 입력 (epoch millis). */
public class DateValidatorEditor implements ValidatorEditor {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder afterField;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder beforeField;

    public DateValidatorEditor() {
        afterField = TextFieldElementBuilder.textField().outlined().label("After (yyyy-MM-dd)");
        beforeField = TextFieldElementBuilder.textField().outlined().label("Before (yyyy-MM-dd)");
        root = div().css("validator-editor", "validator-editor-row")
                .add(afterField)
                .add(beforeField)
                .element();
    }

    @Override
    public void load(AttributeType value) {
        afterField.value(value != null && value.after() != null ? String.valueOf(value.after().longValue()) : "");
        beforeField.value(value != null && value.before() != null ? String.valueOf(value.before().longValue()) : "");
    }

    @Override
    public AttributeType collect() {
        Double after = parseDouble(afterField.value());
        Double before = parseDouble(beforeField.value());
        return AttributeType.date(after, before);
    }

    private static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    @Override
    public HTMLElement element() { return root; }
}

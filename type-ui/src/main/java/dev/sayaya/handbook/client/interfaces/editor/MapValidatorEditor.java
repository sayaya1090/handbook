package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.domain.AttributeTypeValue;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/**
 * Map 타입의 키/값 타입 및 범위 지정 에디터.
 *
 * <p><b>책임:</b> Map의 키/값 타입을 각각 선택하고, 선택된 타입의 ValidatorEditor를
 * 재귀적으로 서브 에디터로 표시한다. 예: Map&lt;Text(^[A-Z]+$), Number(0~100)&gt;</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ValidatorEditorFactory} — 서브 타입 에디터 재귀 생성</li>
 *   <li>{@link SelectElementBuilder} — MD3 Select (sayaya-ui)</li>
 * </ul></p>
 */
public class MapValidatorEditor implements ValidatorEditor {
    private static final String[] ELEMENT_TYPES = {"text", "number", "date", "enum", "bool", "file", "document", "array", "map"};

    private final HTMLDivElement root;
    private final SelectElementBuilder.OutlinedSelectElementBuilder keySelect;
    private final SelectElementBuilder.OutlinedSelectElementBuilder valueSelect;
    private final HTMLDivElement keySubEditorContainer;
    private final HTMLDivElement valueSubEditorContainer;
    private final ValidatorEditorFactory factory;
    private ValidatorEditor keySubEditor;
    private ValidatorEditor valueSubEditor;

    public MapValidatorEditor(ValidatorEditorFactory factory) {
        this.factory = factory;
        keySelect = createSelect("Key type");
        valueSelect = createSelect("Value type");
        keySubEditorContainer = div().css("validator-sub-editor").element();
        valueSubEditorContainer = div().css("validator-sub-editor").element();

        keySelect.onChange(e -> onKeyTypeChanged());
        valueSelect.onChange(e -> onValueTypeChanged());

        root = div().css("validator-editor")
                .add(keySelect)
                .add(keySubEditorContainer)
                .add(valueSelect)
                .add(valueSubEditorContainer)
                .element();
    }

    @Override
    public void load(AttributeTypeValue value) {
        if (value != null && value.keyType != null && value.keyType.type != null) {
            keySelect.selectByValue(value.keyType.type);
            onKeyTypeChanged();
            if (keySubEditor != null) keySubEditor.load(value.keyType);
        } else {
            keySelect.selectByValue("text");
            onKeyTypeChanged();
        }
        if (value != null && value.valueType != null && value.valueType.type != null) {
            valueSelect.selectByValue(value.valueType.type);
            onValueTypeChanged();
            if (valueSubEditor != null) valueSubEditor.load(value.valueType);
        } else {
            valueSelect.selectByValue("text");
            onValueTypeChanged();
        }
    }

    @Override
    public AttributeTypeValue collect() {
        AttributeTypeValue keyType = collectFrom(keySelect, keySubEditor);
        AttributeTypeValue valueType = collectFrom(valueSelect, valueSubEditor);
        return AttributeTypeValue.map(keyType, valueType);
    }

    @Override
    public HTMLElement element() { return root; }

    private AttributeTypeValue collectFrom(SelectElementBuilder.OutlinedSelectElementBuilder select, ValidatorEditor subEditor) {
        if (subEditor != null) return subEditor.collect();
        AttributeTypeValue v = new AttributeTypeValue();
        v.type = select.value();
        return v;
    }

    private void onKeyTypeChanged() {
        keySubEditorContainer.innerHTML = "";
        keySubEditor = factory.create(keySelect.value());
        if (keySubEditor != null) keySubEditorContainer.appendChild(keySubEditor.element());
    }

    private void onValueTypeChanged() {
        valueSubEditorContainer.innerHTML = "";
        valueSubEditor = factory.create(valueSelect.value());
        if (valueSubEditor != null) valueSubEditorContainer.appendChild(valueSubEditor.element());
    }

    private SelectElementBuilder.OutlinedSelectElementBuilder createSelect(String label) {
        SelectElementBuilder.OutlinedSelectElementBuilder select = SelectElementBuilder.select().outlined().label(label);
        for (String t : ELEMENT_TYPES) {
            if (("array".equals(t) || "map".equals(t)) && factory.isMaxDepth()) continue;
            select.option().value(t).text(t).done();
        }
        return select;
    }
}

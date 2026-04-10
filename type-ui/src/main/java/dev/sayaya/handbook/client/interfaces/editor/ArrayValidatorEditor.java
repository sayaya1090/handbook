package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/**
 * Array 타입의 요소 타입 및 범위 지정 에디터.
 *
 * <p><b>책임:</b> 배열 원소 타입을 선택하고, 선택된 타입의 ValidatorEditor를
 * 재귀적으로 서브 에디터로 표시한다. 예: Array&lt;Number(0~100)&gt;, Array&lt;Map&lt;Text, Date&gt;&gt;</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ValidatorEditorFactory} — 서브 타입 에디터 재귀 생성</li>
 *   <li>{@link SelectElementBuilder} — MD3 Select (sayaya-ui)</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 재귀 깊이는 ValidatorEditorFactory가 제한한다 (기본 3단계).</p>
 */
public class ArrayValidatorEditor implements ValidatorEditor {
    private static final String[] ELEMENT_TYPES = {"text", "number", "date", "enum", "bool", "file", "document", "array", "map"};

    private final HTMLDivElement root;
    private final SelectElementBuilder.OutlinedSelectElementBuilder typeSelect;
    private final HTMLDivElement subEditorContainer;
    private final ValidatorEditorFactory factory;
    private ValidatorEditor currentSubEditor;

    public ArrayValidatorEditor(ValidatorEditorFactory factory) {
        this.factory = factory;
        typeSelect = SelectElementBuilder.select().outlined().label("Element type");
        for (String t : ELEMENT_TYPES) {
            if (("array".equals(t) || "map".equals(t)) && factory.isMaxDepth()) continue;
            typeSelect.option().value(t).text(t).done();
        }
        typeSelect.onChange(e -> onTypeChanged());

        subEditorContainer = div().css("validator-sub-editor").element();

        root = div().css("validator-editor")
                .add(typeSelect)
                .add(subEditorContainer)
                .element();
    }

    @Override
    public void load(AttributeTypeValue value) {
        if (value != null && value.elementType != null && value.elementType.type != null) {
            typeSelect.selectByValue(value.elementType.type);
            onTypeChanged();
            if (currentSubEditor != null) {
                currentSubEditor.load(value.elementType);
            }
        } else {
            typeSelect.selectByValue("text");
            onTypeChanged();
        }
    }

    @Override
    public AttributeTypeValue collect() {
        String selected = typeSelect.value();
        AttributeTypeValue elementType;
        if (currentSubEditor != null) {
            elementType = currentSubEditor.collect();
        } else {
            elementType = new AttributeTypeValue();
            elementType.type = selected;
        }
        return AttributeTypeValue.array(elementType);
    }

    @Override
    public HTMLElement element() { return root; }

    private void onTypeChanged() {
        subEditorContainer.innerHTML = "";
        currentSubEditor = factory.create(typeSelect.value());
        if (currentSubEditor != null) {
            subEditorContainer.appendChild(currentSubEditor.element());
        }
    }
}

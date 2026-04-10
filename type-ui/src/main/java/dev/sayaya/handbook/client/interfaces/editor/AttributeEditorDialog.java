package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.domain.AttributeTypeValue;
import dev.sayaya.handbook.client.domain.AttributeValue;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/**
 * 속성 편집 다이얼로그.
 *
 * <p><b>책임:</b> 타입 셀렉터(text/number/date/enum/bool/array/map/file/document 9종),
 * 타입별 validator 에디터({@link ValidatorEditor} 구현체), 이름/설명 입력 필드를 제공하며,
 * Apply 시 편집된 {@link AttributeValue}를 콜백으로 반환한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TextValidatorEditor}, {@link NumberValidatorEditor} 등 — 타입별 검증 에디터</li>
 *   <li>{@link DocumentValidatorEditor} — document 타입 참조 선택 (TypeList 의존)</li>
 *   <li>{@link LabelProvider} — 다국어 레이블</li>
 *   <li>{@link TypeList} — DocumentValidatorEditor에 전달</li>
 * </ul></p>
 * <p><b>주의:</b> 싱글턴이므로 동시에 하나의 다이얼로그만 표시된다.
 * show() 호출 시 현재 속성 값을 로드하고, hide() 시 상태를 초기화한다.</p>
 */
@Singleton
public class AttributeEditorDialog implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder nameField;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder descField;
    private final HTMLDivElement typeSelector;
    private final HTMLDivElement validatorContainer;
    private final Map<String, HTMLElement> typeButtons = new LinkedHashMap<>();
    private final Map<String, ValidatorEditor> validatorEditors = new LinkedHashMap<>();
    private Consumer<AttributeValue> onApply;
    private AttributeValue current;
    private String selectedType = "text";

    @Inject
    AttributeEditorDialog(LabelProvider labelProvider, TypeList typeList) {
        nameField = TextFieldElementBuilder.textField().outlined().label("Name").css("attr-edit-field");
        descField = TextFieldElementBuilder.textField().outlined().label("Description").css("attr-edit-field");

        // Validator 에디터 생성
        validatorEditors.put("text", new TextValidatorEditor());
        validatorEditors.put("number", new NumberValidatorEditor());
        validatorEditors.put("date", new DateValidatorEditor());
        validatorEditors.put("enum", new EnumValidatorEditor());
        validatorEditors.put("file", new FileValidatorEditor());
        validatorEditors.put("document", new DocumentValidatorEditor(typeList));

        // 타입 셀렉터
        typeSelector = div().css("attr-type-selector").element();
        String[] types = {"text", "number", "date", "enum", "bool", "array", "map", "file", "document"};
        for (String type : types) {
            HTMLElement btn = ButtonElementBuilder.button().outlined().text(type).css("attr-type-btn").element();
            btn.addEventListener("click", e -> selectType(type));
            typeButtons.put(type, btn);
            typeSelector.appendChild(btn);
        }

        // Validator 컨테이너
        validatorContainer = div().css("validator-container").element();

        HTMLDivElement headerDiv = div().css("attr-editor-header").element();
        headerDiv.textContent = "Edit Attribute";

        HTMLElement applyBtn = ButtonElementBuilder.button().filled().text("Apply").css("attr-edit-apply").element();
        applyBtn.addEventListener("click", e -> apply());

        HTMLElement closeBtn = ButtonElementBuilder.button().text().text("Close").css("attr-edit-close").element();
        closeBtn.addEventListener("click", e -> hide());

        root = div().css("attr-editor-dialog")
                .add(headerDiv)
                .add(nameField)
                .add(typeSelector)
                .add(validatorContainer)
                .add(descField)
                .add(div().css("attr-editor-actions").add(closeBtn).add(applyBtn))
                .element();
        root.style.setProperty("display", "none");

        labelProvider.subscribe(labels -> {
            nameField.label(labels.getOrDefault("type.attr.name", "Name"));
            descField.label(labels.getOrDefault("type.attr.description", "Description"));
            applyBtn.textContent = labels.getOrDefault("type.attr.apply", "Apply");
            closeBtn.textContent = labels.getOrDefault("type.attr.close", "Close");
        });
    }

    public void show(AttributeValue attribute, Consumer<AttributeValue> onApply) {
        this.current = attribute;
        this.onApply = onApply;
        nameField.value(attribute.name);
        descField.value(attribute.description != null ? attribute.description : "");
        selectedType = attribute.type != null ? attribute.type.type : "text";
        showValidatorEditor(selectedType, attribute.type);
        updateTypeButtons();
        root.style.setProperty("display", "flex");
    }

    public void hide() {
        root.style.setProperty("display", "none");
        current = null;
        onApply = null;
    }

    private void selectType(String type) {
        selectedType = type;
        showValidatorEditor(type, null);
        updateTypeButtons();
    }

    private void showValidatorEditor(String type, AttributeTypeValue value) {
        validatorContainer.innerHTML = "";
        ValidatorEditor editor = validatorEditors.get(type);
        if (editor != null) {
            editor.load(value);
            validatorContainer.appendChild(editor.element());
        }
    }

    private void updateTypeButtons() {
        for (Map.Entry<String, HTMLElement> entry : typeButtons.entrySet()) {
            entry.getValue().toggleAttribute("selected", entry.getKey().equals(selectedType));
        }
    }

    private void apply() {
        if (current == null || onApply == null) return;
        ValidatorEditor editor = validatorEditors.get(selectedType);
        AttributeTypeValue atv;
        if (editor != null) {
            atv = editor.collect();
        } else {
            atv = new AttributeTypeValue();
            atv.type = selectedType;
        }
        AttributeValue updated = current
                .withName(nameField.value())
                .withType(atv)
                .withDescription(descField.value());
        onApply.accept(updated);
        hide();
    }

    @Override
    public HTMLDivElement element() { return root; }
}

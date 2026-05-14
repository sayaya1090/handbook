package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.AttributeType;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.*;
import jsinterop.base.Js;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;

/**
 * 타입의 개별 속성을 편집하는 다이얼로그 (UC-T7).
 */
@Singleton
public class AttributeEditorDialog implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder nameField;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder descField;
    private final HTMLDivElement typeSelector;
    private final HTMLDivElement validatorContainer;
    private final Map<String, HTMLElement> typeButtons = new HashMap<>();
    private final Map<String, ValidatorEditor> validatorEditors;
    private String selectedType = "text";
    private Attribute current;
    private Consumer<Attribute> onApply;

    @Inject
    AttributeEditorDialog(LabelProvider labelProvider, ValidatorEditorFactory validatorFactory) {
        this.validatorEditors = validatorFactory.createAll();

        nameField = TextFieldElementBuilder.textField().outlined().css("attr-edit-field");
        descField = TextFieldElementBuilder.textField().outlined().css("attr-edit-field");

        typeSelector = div().css("attr-type-selector").element();
        validatorContainer = div().css("validator-container").element();

        var headerDiv = div().css("attr-editor-header").id("attr-editor-headline").text("Edit Attribute").element();
        var applyBtn = ButtonElementBuilder.button().filled().css("attr-edit-apply").text("Apply").element();
        var closeBtn = ButtonElementBuilder.button().text().css("attr-edit-close").text("Close").element();

        applyBtn.addEventListener("click", e -> {
            if (current != null && onApply != null) {
                ValidatorEditor editor = validatorEditors.get(selectedType);
                Attribute updated = Attribute.create(
                        current.id(),
                        nameField.value(),
                        current.order(),
                        editor != null ? editor.collect() : AttributeType.create(selectedType)
                );
                updated.description(descField.value());
                onApply.accept(updated);
            }
            hide();
        });
        closeBtn.addEventListener("click", e -> hide());

        root = div().css("attr-editor-dialog")
                .add(headerDiv)
                .add(nameField)
                .add(typeSelector)
                .add(validatorContainer)
                .add(descField)
                .add(div().css("attr-editor-actions").add(closeBtn).add(applyBtn))
                .element();
        root.setAttribute("role", "dialog");
        root.setAttribute("aria-labelledby", "attr-editor-headline");
        root.style.display = "none";

        // Escape 키로 다이얼로그 닫기
        root.addEventListener("keydown", evt -> {
            KeyboardEvent ke = Js.cast(evt);
            if ("Escape".equals(ke.key)) {
                hide();
            }
        });

        String[] typeNames = {"text", "number", "date", "enum", "bool", "array", "map", "file", "document"};
        for (String type : typeNames) {
            var btn = ButtonElementBuilder.button().outlined().css("attr-type-btn").text(type).element();
            btn.addEventListener("click", e -> selectType(type));
            typeButtons.put(type, btn);
            typeSelector.appendChild(btn);
        }

        labelProvider.subscribe(labels -> {
            nameField.label(labels.getOrDefault("type.attr.name", "Name"));
            descField.label(labels.getOrDefault("type.attr.description", "Description"));
            applyBtn.textContent = labels.getOrDefault("type.attr.apply", "Apply");
            closeBtn.textContent = labels.getOrDefault("type.attr.close", "Close");
        });
    }

    public void show(Attribute attribute, Consumer<Attribute> onApply) {
        elemental2.dom.DomGlobal.console.log("[AttributeEditorDialog] show() - attr: " + attribute.name());
        this.current = attribute;
        this.onApply = onApply;
        nameField.value(attribute.name());
        descField.value(attribute.description() != null ? attribute.description() : "");
        selectedType = attribute.type() != null ? attribute.type().type() : "text";
        showValidatorEditor(selectedType, attribute.type());
        updateTypeButtons();
        root.style.setProperty("display", "flex");
        root.style.setProperty("pointer-events", "all");
        root.classList.add("visible");
        // 열린 후 이름 필드에 포커스
        DomGlobal.setTimeout(e -> nameField.element().focus(), 100);
    }

    public void hide() {
        elemental2.dom.DomGlobal.console.log("[AttributeEditorDialog] hide()");
        root.style.setProperty("display", "none");
        root.style.setProperty("pointer-events", "none");
        root.classList.remove("visible");
        current = null;
        onApply = null;
    }

    private void selectType(String type) {
        selectedType = type;
        showValidatorEditor(type, null);
        updateTypeButtons();
    }

    private void showValidatorEditor(String type, AttributeType value) {
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

    @Override
    public HTMLDivElement element() { return root; }
}

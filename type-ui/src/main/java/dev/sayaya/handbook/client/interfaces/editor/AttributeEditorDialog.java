package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.IntegrityAnalysisService;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.AttributeType;
import dev.sayaya.handbook.domain.Type;
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
    private final IntegrityAnalysisService integrityService;
    private final ConflictResolutionDialog resolutionDialog;
    private final ActionManager actionManager;
    private final ChangeTracker tracker;
    private final TypeList typeList;
    private final dev.sayaya.handbook.client.usecase.PositionMap positionMap;
    private final dev.sayaya.handbook.client.usecase.LayoutProvider layoutProvider;

    private String selectedType = "text";
    private Attribute current;
    private Type ownerType;
    private Consumer<Attribute> onApply;

    @Inject
    AttributeEditorDialog(LabelProvider labelProvider, ValidatorEditorFactory validatorFactory,
                          IntegrityAnalysisService integrityService, ConflictResolutionDialog resolutionDialog,
                          ActionManager actionManager, ChangeTracker tracker, TypeList typeList,
                          dev.sayaya.handbook.client.usecase.PositionMap positionMap,
                          dev.sayaya.handbook.client.usecase.LayoutProvider layoutProvider) {
        this.validatorEditors = validatorFactory.createAll();
        this.integrityService = integrityService;
        this.resolutionDialog = resolutionDialog;
        this.actionManager = actionManager;
        this.tracker = tracker;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.layoutProvider = layoutProvider;

        nameField = TextFieldElementBuilder.textField().outlined().css("attr-edit-field");
        descField = TextFieldElementBuilder.textField().outlined().css("attr-edit-field");

        typeSelector = div().css("attr-type-selector").element();
        validatorContainer = div().css("validator-container").element();

        var headerDiv = div().css("attr-editor-header").id("attr-editor-headline").text("Edit Attribute").element();
        var applyBtn = ButtonElementBuilder.button().filled().css("attr-edit-apply").text("Apply").element();
        var closeBtn = ButtonElementBuilder.button().text().css("attr-edit-close").text("Close").element();

        applyBtn.addEventListener("click", e -> apply());
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

    private void extractReferences(AttributeType attrType, java.util.Set<String> refs) {
        if (attrType == null) return;
        if ("document".equals(attrType.type()) && attrType.referencedType() != null) {
            refs.add(attrType.referencedType());
        }
        extractReferences(attrType.elementType(), refs);
        extractReferences(attrType.keyType(), refs);
        extractReferences(attrType.valueType(), refs);
    }

    private void apply() {
        if (current == null || onApply == null) return;
        ValidatorEditor editor = validatorEditors.get(selectedType);
        AttributeType attrType = editor != null ? editor.collect() : AttributeType.create(selectedType);
        Attribute updated = Attribute.create(current.id(), nameField.value(), current.order(), attrType);
        updated.description(descField.value());

        // 참조 무결성 검사 (중첩 구조 모두 포함)
        java.util.Set<String> refs = new java.util.HashSet<>();
        extractReferences(attrType, refs);
        
        for (String refId : refs) {
            var result = integrityService.analyze(ownerType, refId);
            if (!result.valid()) {
                resolutionDialog.show(result.message(), result.proposals(), p -> resolveConflict(p, updated));
                return; // 다이얼로그가 해결책을 제시하므로 여기서 중단
            }
        }

        onApply.accept(updated);
        hide();
    }

    private void resolveConflict(IntegrityAnalysisService.ResolutionProposal p, Attribute updatedAttr) {
        if (p.targetIsOwner()) {
            // 1. 소유자 타입 기간 조정 액션 생성
            dev.sayaya.handbook.client.usecase.action.EditTBoxDateAction a1 = new dev.sayaya.handbook.client.usecase.action.EditTBoxDateAction(typeList, tracker, ownerType, p.newStart(), p.newEnd());
            Type correctedOwner = a1.getAfter();
            
            // 2. 조정된 타입에 속성을 추가/수정하는 액션 생성
            Type finalType = applyAttributeToType(correctedOwner, updatedAttr);
            dev.sayaya.handbook.client.usecase.action.EditBoxAction a2 = new dev.sayaya.handbook.client.usecase.action.EditBoxAction(typeList, positionMap, tracker, layoutProvider, correctedOwner, finalType);
            
            actionManager.execute(new dev.sayaya.handbook.client.usecase.action.ComplexAction(a1, a2));
        } else {
            // 1. 참조 대상 타입 기간 확장 액션 생성
            Type refType = typeList.getValue().stream().filter(t -> t.id().equals(updatedAttr.type().referencedType())).findFirst().get();
            dev.sayaya.handbook.client.usecase.action.EditTBoxDateAction a1 = new dev.sayaya.handbook.client.usecase.action.EditTBoxDateAction(typeList, tracker, refType, p.newStart(), p.newEnd());
            
            // 2. 소유자 타입에 속성을 추가/수정하는 액션 생성
            Type finalType = applyAttributeToType(ownerType, updatedAttr);
            dev.sayaya.handbook.client.usecase.action.EditBoxAction a2 = new dev.sayaya.handbook.client.usecase.action.EditBoxAction(typeList, positionMap, tracker, layoutProvider, ownerType, finalType);
            
            actionManager.execute(new dev.sayaya.handbook.client.usecase.action.ComplexAction(a1, a2));
        }
        hide();
    }

    /** 타입 객체에 속성을 지능적으로 추가하거나 수정한다. */
    private Type applyAttributeToType(Type target, Attribute attr) {
        java.util.List<Attribute> nextList = new java.util.ArrayList<>();
        boolean found = false;
        if (target.attributes() != null) {
            for (Attribute a : target.attributes()) {
                // ID가 있거나 이름/순서가 같은 경우 '수정'으로 판단
                boolean isMatch = (attr.id() != null && attr.id().equals(a.id())) 
                               || (attr.name().equals(a.name()) && attr.order() == a.order());
                if (isMatch) {
                    nextList.add(attr);
                    found = true;
                } else {
                    nextList.add(a);
                }
            }
        }
        if (!found) nextList.add(attr);
        return target.withAttributes(nextList.toArray(new Attribute[0]));
    }

    public void show(Type owner, Attribute attribute, Consumer<Attribute> onApply) {
        elemental2.dom.DomGlobal.console.log("[AttributeEditorDialog] show() - owner: " + owner.id() + ", attr: " + attribute.name());
        this.ownerType = owner;
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

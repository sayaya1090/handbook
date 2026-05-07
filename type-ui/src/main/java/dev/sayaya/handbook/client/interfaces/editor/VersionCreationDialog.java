package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.DateFormatter;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.SchemaEvolutionAction;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 타입의 새 버전을 생성하는 다이얼로그 (UC-T27).
 * 
 * <p><b>책임:</b> 새로운 개시 일시와 버전명을 입력받고 스키마 진화 액션을 트리거한다.</p>
 */
@Singleton
public class VersionCreationDialog implements IsElement<HTMLElement> {
    private final DialogElementBuilder _this;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder effectInput;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder versionInput;
    
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final LayoutProvider layoutProvider;
    private final LayoutList layoutList;
    private final SelectedBoxElement selection;
    private Type targetType;

    @Inject
    VersionCreationDialog(TypeRepository typeRepository, LayoutRepository layoutRepository,
                          TypeList typeList, ChangeTracker tracker, ActionManager actionManager,
                          LayoutProvider layoutProvider, LayoutList layoutList, SelectedBoxElement selection) {
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.layoutProvider = layoutProvider;
        this.layoutList = layoutList;
        this.selection = selection;

        effectInput = TextFieldElementBuilder.textField().outlined()
                .attr("id", "version-creation-effect")
                .label("New Version Start Date (YYYY-MM-DD)");
        
        versionInput = TextFieldElementBuilder.textField().outlined()
                .attr("id", "version-creation-version")
                .label("New Version Name");

        _this = DialogElementBuilder.dialog()
                .attr("id", "version-creation-dialog")
                .headline("Create New Version")
                .content(div().add(effectInput).add(versionInput))
                .actions(ButtonElementBuilder.button().filled().attr("id", "version-creation-submit").text("Create").on(EventType.click, e -> submit()));
        
        _this.actions(ButtonElementBuilder.button().text().attr("id", "version-creation-close").text("Cancel").on(EventType.click, e -> _this.close()));
    }

    public void show(Type type) {
        elemental2.dom.DomGlobal.console.log("[VersionCreationDialog] show() - type: " + type.key());
        elemental2.dom.DomGlobal.console.log("[VersionCreationDialog] Attached to DOM: " + (_this.element().parentNode != null));
        this.targetType = type;
        // 기본값으로 현재 시각 + 1일
        effectInput.value(DateFormatter.format(System.currentTimeMillis() + 86400000L));
        versionInput.value(type.version() + ".1");
        _this.show();
    }

    private void submit() {
        if (targetType == null) return;
        String effectVal = effectInput.value();
        String newVersionName = versionInput.value();
        elemental2.dom.DomGlobal.console.log("[VersionCreationDialog] submit() - effect: " + effectVal + ", version: " + newVersionName);
        
        double newEffect = DateFormatter.parse(effectVal);
        
        Type updatedType = Type.create(targetType.id(), newVersionName, newEffect, 253402214400000.0);
        updatedType.description(targetType.description());
        updatedType.primitive(targetType.primitive());
        updatedType.parent(targetType.parent());
        updatedType.attributes(targetType.attributes());

        actionManager.execute(new SchemaEvolutionAction(
                typeRepository, layoutRepository, typeList, layoutProvider, layoutList,
                tracker, actionManager, selection, layoutProvider.getValue(), targetType, newEffect, updatedType
        ));
        _this.close();
    }

    @Override
    public HTMLElement element() { return _this.element(); }
}

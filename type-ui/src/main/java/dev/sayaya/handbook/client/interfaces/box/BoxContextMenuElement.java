package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.domain.AttributeTypeValue;
import dev.sayaya.handbook.domain.AttributeValue;
import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.handbook.client.interfaces.ContextMenuHelper;
import dev.sayaya.handbook.client.interfaces.editor.AttributeEditorDialog;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.client.usecase.action.EditBoxAction;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static dev.sayaya.handbook.client.interfaces.ContextMenuHelper.menuItem;
import static org.jboss.elemento.Elements.div;


/**
 * 타입 박스 우클릭 시 표시되는 컨텍스트 메뉴.
 *
 * <p><b>책임:</b> 대상 타입 박스에 "속성 추가", "버전 히스토리", "삭제" 메뉴 항목을 표시하며,
 * 속성 추가 시 {@link AttributeEditorDialog}를 열고 {@link EditBoxAction}으로 속성을 추가한다.
 * 버전 히스토리 시 {@link VersionHistoryPanel}을 열고,
 * 삭제 시 {@link DeleteBoxAction}을 실행한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ActionManager} — 액션 실행</li>
 *   <li>{@link TypeList} — 대상 타입 조회</li>
 *   <li>{@link ChangeTracker} — 변경 상태 마킹</li>
 *   <li>{@link AttributeEditorDialog} — 속성 편집 다이얼로그</li>
 *   <li>{@link VersionHistoryPanel} — 타입 버전 히스토리 패널</li>
 *   <li>{@link LabelProvider} — 다국어 메뉴 텍스트</li>
 * </ul></p>
 * <p><b>주의:</b> show() 호출 시 대상 typeKey를 설정하고, document 클릭 시 자동으로 hide()된다.</p>
 */
@Singleton
public class BoxContextMenuElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final ActionManager actionManager;
    private final TypeList typeList;
    private final ChangeTracker tracker;
    private final SelectedBoxElement selection;
    private final AttributeEditorDialog editorDialog;
    private final VersionHistoryPanel versionHistoryPanel;
    private String targetTypeKey;

    @Inject
    BoxContextMenuElement(ActionManager actionManager, TypeList typeList, ChangeTracker tracker,
                          SelectedBoxElement selection, AttributeEditorDialog editorDialog,
                          VersionHistoryPanel versionHistoryPanel,
                          LabelProvider labelProvider) {
        this.actionManager = actionManager;
        this.typeList = typeList;
        this.tracker = tracker;
        this.selection = selection;
        this.editorDialog = editorDialog;
        this.versionHistoryPanel = versionHistoryPanel;

        HTMLElement addAttrItem = menuItem("Add Attribute");
        HTMLElement versionHistoryItem = menuItem("Version history");
        HTMLElement deleteItem = menuItem("Delete");

        addAttrItem.addEventListener("click", e -> { addAttribute(); hide(); });
        versionHistoryItem.addEventListener("click", e -> { showVersionHistory(); hide(); });
        deleteItem.addEventListener("click", e -> { deleteTarget(); hide(); });

        labelProvider.subscribe(labels -> {
            addAttrItem.textContent = labels.getOrDefault("type.attr.add", "Add Attribute");
            versionHistoryItem.textContent = labels.getOrDefault("type.version_history", "Version history");
            deleteItem.textContent = labels.getOrDefault("type.remove", "Delete");
        });

        root = div().css("ctx-menu")
                .add(addAttrItem)
                .add(versionHistoryItem)
                .add(div().css("ctx-divider"))
                .add(deleteItem)
                .element();
        root.style.setProperty("display", "none");

        DomGlobal.document.addEventListener("click", e -> hide());
    }

    public void show(int x, int y, String typeKey) {
        targetTypeKey = typeKey;
        root.style.setProperty("display", "flex");
        root.style.setProperty("left", x + "px");
        root.style.setProperty("top", y + "px");
    }

    public void hide() {
        root.style.setProperty("display", "none");
        targetTypeKey = null;
    }

    private void addAttribute() {
        TypeValue type = findType();
        if (type == null) return;
        int nextOrder = (type.attributes != null ? type.attributes.length : 0) + 1;
        AttributeValue newAttr = AttributeValue.of("attr-" + nextOrder, nextOrder, AttributeTypeValue.text());
        editorDialog.show(newAttr, applied -> {
            TypeValue before = type;
            AttributeValue[] oldAttrs = before.attributes != null ? before.attributes : new AttributeValue[0];
            AttributeValue[] newAttrs = Arrays.copyOf(oldAttrs, oldAttrs.length + 1);
            newAttrs[oldAttrs.length] = applied;
            TypeValue after = before.withAttributes(newAttrs);
            actionManager.execute(new EditBoxAction(typeList, tracker, before, after));
        });
    }

    private void showVersionHistory() {
        TypeValue type = findType();
        if (type == null) return;
        versionHistoryPanel.show(type.id);
    }

    private void deleteTarget() {
        if (targetTypeKey == null) return;
        for (TypeValue type : typeList.getValue()) {
            if (type.key().equals(targetTypeKey)) {
                actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                break;
            }
        }
    }

    private TypeValue findType() {
        if (targetTypeKey == null) return null;
        for (TypeValue type : typeList.getValue()) {
            if (type.key().equals(targetTypeKey)) return type;
        }
        return null;
    }

    @Override
    public HTMLDivElement element() { return root; }
}

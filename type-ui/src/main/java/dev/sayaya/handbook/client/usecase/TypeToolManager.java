package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.action.DeleteBoxAction;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.client.usecase.action.SaveAction;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.ToolProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Type-UI의 모든 도구를 관리하고 쉘의 Tool Rail과 연동한다.
 */
@Singleton
public class TypeToolManager {
    private final ToolProvider toolProvider;
    private final ActionManager actionManager;
    private final ChangeTracker tracker;
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final LayoutProvider layoutProvider;
    private final LayoutList layoutList;
    private final dev.sayaya.handbook.client.components.ToastContainer toastContainer;
    private final LabelProvider labelProvider;
    private final CanvasMode canvasMode;
    private final GridSnap gridSnap;
    private final SelectedBoxElement selection;
    private final ConfirmDialog confirmDialog;
    private Labels currentLabels = Labels.empty();

    @Inject
    TypeToolManager(ToolProvider toolProvider, ActionManager actionManager, ChangeTracker tracker,
                    TypeRepository typeRepository, LayoutRepository layoutRepository, TypeList typeList,
                    PositionMap positionMap, LayoutProvider layoutProvider, LayoutList layoutList,
                    dev.sayaya.handbook.client.components.ToastContainer toastContainer,
                    LabelProvider labelProvider, CanvasMode canvasMode, GridSnap gridSnap,
                    SelectedBoxElement selection, ConfirmDialog confirmDialog) {
        this.toolProvider = toolProvider;
        this.actionManager = actionManager;
        this.tracker = tracker;
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.layoutProvider = layoutProvider;
        this.layoutList = layoutList;
        this.toastContainer = toastContainer;
        this.labelProvider = labelProvider;
        this.canvasMode = canvasMode;
        this.gridSnap = gridSnap;
        this.selection = selection;
        this.confirmDialog = confirmDialog;
    }

    public void init() {
        // 도구 목록 발행 (초기)
        publishTools();
        
        // 상태 변경 시 도구 목록 재발행 (활성화/비활성화 상태 반영 등)
        actionManager.canUndo().subscribe(v -> publishTools());
        actionManager.canRedo().subscribe(v -> publishTools());
        tracker.hasChangesObservable().subscribe(v -> publishTools());
        canvasMode.observable().subscribe(v -> publishTools());
        gridSnap.enabled().subscribe(v -> publishTools());
        selection.subscribe(v -> publishTools());
        
        labelProvider.subscribe(labels -> {
            this.currentLabels = labels;
            publishTools();
        });

        // 쉘에서의 선택 이벤트 처리
        toolProvider.onSelect(id -> {
            switch (id) {
                case "add": executeAdd(); break;
                case "remove": executeRemove(); break;
                case "bulk-delete": executeBulkDelete(); break;
            }
        });
    }

    private void publishTools() {
        List<Tool> tools = new ArrayList<>();
        
        // 생성 도구
        tools.add(Tool.builder().id("add").icon("fa-plus").title(currentLabels.getOrDefault("type.add", "Add")).order("020").build());
        
        // 삭제 도구 (선택 상태에 따라 분기하거나 둘 다 노출)
        Set<String> selected = selection.getValue();
        if (selected.size() > 1) {
            tools.add(Tool.builder().id("bulk-delete").icon("fa-trash-can-list")
                    .title(currentLabels.getOrDefault("type.bulk_delete", "Bulk Delete")).order("030").build());
        } else if (selected.size() == 1) {
            tools.add(Tool.builder().id("remove").icon("fa-trash")
                    .title(currentLabels.getOrDefault("type.remove", "Remove")).order("030").build());
        }

        toolProvider.publish(tools.toArray(new Tool[0]));
    }

    public void executeAdd() {
        var period = layoutProvider.getValue();
        if (period == null) {
            elemental2.dom.DomGlobal.console.warn("[handbook-error] Cannot add type: No active layout period.");
            return;
        }
        String id = dev.sayaya.handbook.client.interfaces.ContextMenuHelper.uniqueTypeId(typeList);
        dev.sayaya.handbook.domain.Type newType = dev.sayaya.handbook.domain.Type.create(id, "1.0", period.effectDateTime(), period.expireDateTime());
        dev.sayaya.handbook.domain.Position pos = dev.sayaya.handbook.domain.Position.of(50, 80, 240, 160);
        actionManager.execute(new dev.sayaya.handbook.client.usecase.action.ComplexAction(
                new dev.sayaya.handbook.client.usecase.action.CreateBoxAction(typeList, positionMap, tracker, newType, pos),
                new dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction(positionMap, newType.key(), 10)
        ));
    }

    public void executeRemove() {
        Set<String> selected = selection.getValue();
        if (selected.isEmpty()) return;
        String headline = currentLabels.getOrDefault("confirm.delete", "Are you sure you want to delete?");
        String yes = currentLabels.getOrDefault("confirm.yes", "Delete");
        String no = currentLabels.getOrDefault("confirm.no", "Cancel");
        confirmDialog.show(headline, new String[]{no, yes}, option -> {
            if (!option.equals(yes)) return;
            for (Type type : typeList.getValue()) {
                if (selected.contains(type.key())) {
                    actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
                }
            }
        });
    }

    public void executeBulkDelete() {
        Set<String> selected = selection.getValue();
        if (selected.isEmpty()) return;

        String headline = currentLabels.getOrDefault("confirm.delete", "Are you sure you want to delete?");
        String yes = currentLabels.getOrDefault("confirm.yes", "Delete");
        String no = currentLabels.getOrDefault("confirm.no", "Cancel");
        confirmDialog.show(headline, new String[]{no, yes}, option -> {
            if (!option.equals(yes)) return;

            List<Type> toDelete = new ArrayList<>();
            for (Type type : typeList.getValue()) {
                if (selected.contains(type.key())) {
                    toDelete.add(type);
                }
            }
            for (Type type : toDelete) {
                actionManager.execute(new DeleteBoxAction(typeList, tracker, type));
            }
            selection.clear();
        });
    }

    public void executeSave() {
        new SaveAction(typeRepository, layoutRepository, typeList, positionMap, tracker, actionManager, layoutProvider, toastContainer, currentLabels).execute();
    }

    public void executeReload() {
        new LoadAction(typeRepository, layoutRepository, typeList, positionMap, tracker, actionManager, layoutProvider, layoutList).execute();
    }
}

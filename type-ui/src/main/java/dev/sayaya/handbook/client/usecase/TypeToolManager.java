package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.client.usecase.action.SaveAction;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.ToolProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

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
    private Labels currentLabels = Labels.empty();

    @Inject
    TypeToolManager(ToolProvider toolProvider, ActionManager actionManager, ChangeTracker tracker,
                    TypeRepository typeRepository, LayoutRepository layoutRepository, TypeList typeList,
                    PositionMap positionMap, LayoutProvider layoutProvider, LayoutList layoutList,
                    dev.sayaya.handbook.client.components.ToastContainer toastContainer,
                    LabelProvider labelProvider, CanvasMode canvasMode, GridSnap gridSnap) {
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
        
        labelProvider.subscribe(labels -> {
            this.currentLabels = labels;
            publishTools();
        });

        // 쉘에서의 선택 이벤트 처리
        toolProvider.onSelect(id -> {
            switch (id) {
                case "add": executeAdd(); break;
                case "undo": actionManager.undo(); break;
                case "redo": actionManager.redo(); break;
                case "save": executeSave(); break;
                case "reload": executeReload(); break;
                case "mode-layout": canvasMode.setMode(CanvasMode.Mode.LAYOUT); break;
                case "mode-type": canvasMode.setMode(CanvasMode.Mode.TYPE); break;
                case "snap": gridSnap.setEnabled(!gridSnap.isEnabled()); break;
            }
        });
    }

    private void publishTools() {
        List<Tool> tools = new ArrayList<>();
        
        // 모드 전환
        tools.add(Tool.builder().id("mode-layout").icon("fa-arrows-up-down-left-right")
                .title(currentLabels.getOrDefault("type.mode.layout", "Layout Mode")).order("010").build());
        tools.add(Tool.builder().id("mode-type").icon("fa-pen")
                .title(currentLabels.getOrDefault("type.mode.type", "Type Mode")).order("011").build());
        
        // 기본 액션
        tools.add(Tool.builder().id("add").icon("fa-plus").title(currentLabels.getOrDefault("type.add", "Add")).order("020").build());
        
        // 편집 액션
        tools.add(Tool.builder().id("undo").icon("fa-undo").title(currentLabels.getOrDefault("type.undo", "Undo")).order("030").build());
        tools.add(Tool.builder().id("redo").icon("fa-redo").title(currentLabels.getOrDefault("type.redo", "Redo")).order("031").build());
        
        // 스냅
        tools.add(Tool.builder().id("snap").icon("fa-grid-round").title(currentLabels.getOrDefault("type.snap", "Grid Snap")).order("040").build());
        
        // 저장/동기화
        tools.add(Tool.builder().id("save").icon("fa-save").title(currentLabels.getOrDefault("type.save", "Save")).order("050").build());
        tools.add(Tool.builder().id("reload").icon("fa-sync").title(currentLabels.getOrDefault("type.reload", "Reload")).order("051").build());

        toolProvider.publish(tools.toArray(new Tool[0]));
    }

    private void executeAdd() {
        var period = layoutProvider.getValue();
        if (period == null) return;
        String id = dev.sayaya.handbook.client.interfaces.ContextMenuHelper.uniqueTypeId(typeList);
        dev.sayaya.handbook.client.domain.TypeValue newType = dev.sayaya.handbook.client.domain.TypeValue.create(id, "1.0", period.effectDateTime, period.expireDateTime);
        dev.sayaya.handbook.client.domain.Position pos = dev.sayaya.handbook.client.domain.Position.of(50, 80, 240, 160);
        actionManager.execute(new dev.sayaya.handbook.client.usecase.action.ComplexAction(
                new dev.sayaya.handbook.client.usecase.action.CreateBoxAction(typeList, positionMap, tracker, newType, pos),
                new dev.sayaya.handbook.client.usecase.action.PushOutOverlapAction(positionMap, newType.key(), 10)
        ));
    }

    private void executeSave() {
        new SaveAction(typeRepository, layoutRepository, typeList, positionMap, tracker, actionManager, layoutProvider, toastContainer, currentLabels).execute();
    }

    private void executeReload() {
        new LoadAction(typeRepository, layoutRepository, typeList, positionMap, tracker, actionManager, layoutProvider, layoutList).execute();
    }
}

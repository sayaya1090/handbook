package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Type;

/**
 * 스키마 진화(새 버전 생성)를 처리하는 복합 액션.
 * 
 * <p><b>책임:</b>
 * 1. 현재 레이아웃 기간을 분할(Split)하여 새로운 미래 구간을 생성한다.
 * 2. 기존 레이아웃과 선택된 타입의 현재 버전을 '분할 시점'에 마감한다.
 * 3. 선택된 타입의 새 버전 레코드를 생성한다.
 * 4. 처리가 완료되면 새로운 레이아웃 기간으로 화면을 이동시킨다.
 * </p>
 */
public class SchemaEvolutionAction implements Action {
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final LayoutProvider layoutProvider;
    private final LayoutList layoutList;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final SelectedBoxElement selection;
    
    private final LayoutPeriod currentPeriod;
    private final Type targetType;
    private final double splitTime;
    private final Type updatedType;

    public SchemaEvolutionAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                                 TypeList typeList, LayoutProvider layoutProvider, LayoutList layoutList,
                                 ChangeTracker tracker, ActionManager actionManager, SelectedBoxElement selection,
                                 LayoutPeriod currentPeriod, Type targetType, double splitTime, Type updatedType) {
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.layoutProvider = layoutProvider;
        this.layoutList = layoutList;
        this.tracker = tracker;
        this.actionManager = actionManager;
        this.selection = selection;
        this.currentPeriod = currentPeriod;
        this.targetType = targetType;
        this.splitTime = splitTime;
        this.updatedType = updatedType;
    }

    @Override
    public void execute() {
        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] Starting evolution for " + targetType.key());
        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] Split time: " + splitTime);
        
        // 1. 기존 기간 마감 (expireDateTime 설정)
        Type closedType = Type.create(targetType.id(), targetType.version(), targetType.effectDateTime(), splitTime);
        closedType.description(targetType.description());
        closedType.primitive(targetType.primitive());
        closedType.parent(targetType.parent());
        closedType.attributes(targetType.attributes());
        closedType.width(targetType.width());
        closedType.height(targetType.height());
        
        // 2. 신규 기간 생성
        LayoutPeriod newPeriod = LayoutPeriod.of(splitTime, 253402214400000.0);
        
        // 3. 새 버전 타입 생성
        Type nextVersion = Type.create(targetType.id(), updatedType.version(), splitTime, 253402214400000.0);
        nextVersion.description(updatedType.description());
        nextVersion.primitive(updatedType.primitive());
        nextVersion.parent(updatedType.parent());
        nextVersion.attributes(updatedType.attributes());
        nextVersion.width(targetType.width());
        nextVersion.height(targetType.height());

        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] New version created: " + nextVersion.key());

        // 4. 완료 후 새 기간으로 내비게이션 및 타입 리스트 갱신
        layoutList.replace(java.util.List.of(currentPeriod, newPeriod)); 
        typeList.update(targetType, closedType); // 기존 타입 마감 반영
        typeList.add(nextVersion);               // 신규 버전 추가
        
        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] Navigating to new period...");
        layoutProvider.replace(newPeriod);
        
        // 스택 초기화 (진화는 되돌릴 수 없는 역사적 변경)
        actionManager.clear();
        tracker.reset();
        
        // 새 버전으로 선택 상태 변경 (UI 연동)
        selection.clear();
        selection.select(nextVersion.key());
        
        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] Evolution complete.");
    }

    @Override
    public void rollback() {
        // 스키마 진화는 되돌릴 수 없음 (no-op)
    }
}

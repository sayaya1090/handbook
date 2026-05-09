package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 스키마 진화(새 버전 생성)를 처리하는 복합 액션.
 * 
 * <p><b>책임:</b>
 * 1. 현재 레이아웃 기간을 분할(Split)하여 새로운 미래 구간을 생성한다.
 * 2. 기존 레이아웃과 선택된 타입의 현재 버전을 '분할 시점'에 마감한다.
 * 3. 선택된 타입의 새 버전 레코드를 생성한다.
 * 4. 기존 레이아웃의 모든 위치 정보를 새 기간으로 상속한다.
 * 5. 처리가 완료되면 새로운 레이아웃 기간으로 화면을 이동시킨다.
 * </p>
 */
public class SchemaEvolutionAction implements Action {
    private final TypeRepository typeRepository;
    private final LayoutRepository layoutRepository;
    private final TypeList typeList;
    private final LayoutProvider layoutProvider;
    private final LayoutList layoutList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final ActionManager actionManager;
    private final SelectedBoxElement selection;
    
    private final LayoutPeriod currentPeriod;
    private final Type targetType;
    private final double splitTime;
    private final Type updatedType;

    public SchemaEvolutionAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                                 TypeList typeList, LayoutProvider layoutProvider, LayoutList layoutList,
                                 PositionMap positionMap, ChangeTracker tracker, ActionManager actionManager,
                                 SelectedBoxElement selection, LayoutPeriod currentPeriod,
                                 Type targetType, double splitTime, Type updatedType) {
        this.typeRepository = typeRepository;
        this.layoutRepository = layoutRepository;
        this.typeList = typeList;
        this.layoutProvider = layoutProvider;
        this.layoutList = layoutList;
        this.positionMap = positionMap;
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
        // 기존 LayoutPeriod 마감
        LayoutPeriod closedPeriod = LayoutPeriod.of(currentPeriod.effectDateTime(), splitTime);
        
        // 기존 타입 버전 마감
        Type closedType = Type.create(targetType.id(), targetType.version(), targetType.effectDateTime(), splitTime);
        closedType.description(targetType.description());
        closedType.primitive(targetType.primitive());
        closedType.parent(targetType.parent());
        closedType.attributes(targetType.attributes());
        closedType.width(targetType.width());
        closedType.height(targetType.height());
        
        // 2. 신규 기간 생성
        LayoutPeriod newPeriod = LayoutPeriod.of(splitTime, currentPeriod.expireDateTime());
        
        // 3. 새 버전 타입 생성
        Type nextVersion = Type.create(targetType.id(), updatedType.version(), splitTime, 253402214400000.0);
        nextVersion.description(updatedType.description());
        nextVersion.primitive(updatedType.primitive());
        nextVersion.parent(updatedType.parent());
        nextVersion.attributes(updatedType.attributes());
        nextVersion.width(updatedType.width());
        nextVersion.height(updatedType.height());

        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] New version created: " + nextVersion.key());

        // 4. 레이아웃 상속 (X, Y 좌표 복사)
        Map<String, Position> currentPositions = positionMap.getValue();
        Map<String, Position> inheritedPositions = new LinkedHashMap<>();
        for (Map.Entry<String, Position> entry : currentPositions.entrySet()) {
            String key = entry.getKey();
            if (key.equals(targetType.key())) {
                // 진화 대상 타입은 새 버전의 키로 위치 계승
                inheritedPositions.put(nextVersion.key(), entry.getValue());
            } else {
                // 나머지 타입들은 그대로 유지
                inheritedPositions.put(key, entry.getValue());
            }
        }

        // 5. 상태 갱신
        // 레이아웃 목록 갱신 (기존 목록에서 현재 기간을 마감된 기간으로 교체하고 새 기간 추가)
        java.util.List<LayoutPeriod> periods = new ArrayList<>(layoutList.getValue());
        int currentIndex = periods.indexOf(currentPeriod);
        if (currentIndex >= 0) {
            periods.set(currentIndex, closedPeriod);
            periods.add(currentIndex + 1, newPeriod);
        } else {
            periods.add(newPeriod);
        }
        layoutList.replace(periods); 

        // 타입 목록 갱신
        typeList.update(targetType, closedType); // 기존 타입 마감 반영
        typeList.add(nextVersion);               // 신규 버전 추가
        
        // 위치 정보 갱신 (새 기간으로 가기 전에 미리 채워넣음)
        positionMap.replace(inheritedPositions);
        
        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] Navigating to new period...");
        layoutProvider.replace(newPeriod);
        
        // 스택 초기화 금지 (UI-only 모드에서는 'Save' 버튼을 눌러야 하므로 dirty 상태 유지)
        
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

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
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;
import dev.sayaya.handbook.domain.TypeLayout;

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
    
    private final TypeLayout currentLayout;
    private final Type targetType;
    private final double splitTime;
    private final Type updatedType;

    public SchemaEvolutionAction(TypeRepository typeRepository, LayoutRepository layoutRepository,
                                 TypeList typeList, LayoutProvider layoutProvider, LayoutList layoutList,
                                 PositionMap positionMap, ChangeTracker tracker, ActionManager actionManager,
                                 SelectedBoxElement selection, TypeLayout currentLayout,
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
        this.currentLayout = currentLayout;
        this.targetType = targetType;
        this.splitTime = splitTime;
        this.updatedType = updatedType;
    }

    @Override
    public void execute() {
        elemental2.dom.DomGlobal.console.log("[SchemaEvolutionAction] Starting evolution for " + targetType.key());
        
        // 1. 기존 기간 마감 (expireDateTime 설정)
        TypeLayout closedLayout = TypeLayout.create(currentLayout.id(), currentLayout.workspace(), currentLayout.effectDateTime(), splitTime, currentLayout.positions());
        closedLayout.rev(currentLayout.rev()); // 낙관적 잠금 보존
        
        Type closedType = Type.create(targetType.id(), targetType.version(), targetType.effectDateTime(), splitTime);
        closedType.description(targetType.description());
        closedType.primitive(targetType.primitive());
        closedType.parent(targetType.parent());
        closedType.attributes(targetType.attributes());
        closedType.width(targetType.width());
        closedType.height(targetType.height());
        closedType.rev(targetType.rev()); // 낙관적 잠금 보존
        
        // 2. 신규 기간 생성
        TypeLayout newLayout = TypeLayout.create(null, currentLayout.workspace(), splitTime, currentLayout.expireDateTime(), currentLayout.positions());
        
        // 3. 새 버전 타입 생성
        Type nextVersion = Type.create(targetType.id(), updatedType.version(), splitTime, 253402214400000.0);
        nextVersion.description(updatedType.description());
        nextVersion.primitive(updatedType.primitive());
        nextVersion.parent(updatedType.parent());
        
        // 중요: 새 버전의 속성들은 기존 버전의 ID를 들고 있으면 안 됨 (DB PK 충돌 방지)
        if (updatedType.attributes() != null) {
            dev.sayaya.handbook.domain.Attribute[] clonedAttrs = new dev.sayaya.handbook.domain.Attribute[updatedType.attributes().length];
            for (int i = 0; i < updatedType.attributes().length; i++) {
                clonedAttrs[i] = updatedType.attributes()[i].cloneWithoutId();
            }
            nextVersion.attributes(clonedAttrs);
        }
        
        nextVersion.width(updatedType.width());
        nextVersion.height(updatedType.height());

        // 4. 레이아웃 상속
        Map<String, Position> currentPositions = positionMap.getValue();
        Map<String, Position> inheritedPositions = new LinkedHashMap<>(currentPositions);
        Position pos = currentPositions.get(targetType.key());
        if (pos != null) {
            inheritedPositions.put(nextVersion.key(), pos);
        }

        // 5. 변경 상태 마킹 (UI 하이라이트 및 저장 대상 지정) - UI 렌더링 전 먼저 마킹
        tracker.markChanged(closedType.key());
        tracker.markChanged(nextVersion.key());
        tracker.markChanged(nextVersion.key() + ":version");
        
        // 기존 버전에서 변경되었던 필드들의 하이라이트 상태와 원본 값을 새 버전으로 상속
        tracker.inherit(targetType.key() + ":description", nextVersion.key() + ":description");
        if (targetType.attributes() != null) {
            for (dev.sayaya.handbook.domain.Attribute attr : targetType.attributes()) {
                tracker.inherit(targetType.key() + ":attr:" + attr.name(), nextVersion.key() + ":attr:" + attr.name());
            }
        }

        tracker.markChanged("LAYOUT:" + currentLayout.id());
        tracker.markChanged("LAYOUT:" + newLayout.id());

        // 6. 상태 갱신 - 순서가 매우 중요함 (PeriodRecalculationService의 자동 발화 및 선택 유도)
        
        // 6.1. 위치 맵 상속 적용 (가장 먼저 적용해야 자동 선택 후 화면 렌더링 시 좌표를 찾음)
        positionMap.replace(inheritedPositions);
        
        // 6.2. 선택 상태를 새 버전으로 변경
        selection.clear();
        selection.select(nextVersion.key());
        
        // 6.3. 레이아웃 목록 초기화
        java.util.List<TypeLayout> layouts = new ArrayList<>(layoutList.getValue());
        int currentIndex = -1;
        for (int i = 0; i < layouts.size(); i++) {
            TypeLayout l = layouts.get(i);
            if (l.id() != null && l.id().equals(currentLayout.id())) {
                currentIndex = i;
                break;
            } else if (l == currentLayout) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex >= 0) {
            layouts.set(currentIndex, closedLayout);
            layouts.add(currentIndex + 1, newLayout);
        } else {
            layouts.add(newLayout);
        }
        layoutList.replace(layouts); 

        // 6.4. 타입 리스트 갱신 (원자적 처리: PeriodRecalculationService가 중간 상태로 인해 레이아웃을 유실하지 않도록 1회만 발화)
        java.util.Set<Type> nextTypes = new java.util.LinkedHashSet<>(typeList.getValue());
        nextTypes.removeIf(t -> t.key().equals(targetType.key()));
        nextTypes.add(closedType);
        nextTypes.add(nextVersion);
        typeList.replace(nextTypes);
    }

    @Override
    public void rollback() {
        // 스키마 진화는 되돌릴 수 없음 (no-op)
    }
}

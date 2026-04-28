package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.TypeValue;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;

/**
 * 새 타입을 캔버스에 추가하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 TypeList에 타입을 추가하고 PositionMap에 위치를 설정하며
 * ChangeTracker에 변경을 마킹한다. rollback 시 타입을 제거하고 마킹을 해제한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeList} — 타입 추가/제거</li>
 *   <li>{@link PositionMap} — 위치 설정</li>
 *   <li>{@link ChangeTracker} — 변경 상태 마킹/해제</li>
 * </ul></p>
 * <p><b>주의:</b> 보통 {@link PushOutOverlapAction}과 {@link ComplexAction}으로 묶여 겹침을 해소한다.</p>
 */
public class CreateBoxAction implements Action {
    private final TypeList typeList;
    private final PositionMap positionMap;
    private final ChangeTracker tracker;
    private final TypeValue type;
    private final Position position;

    public CreateBoxAction(TypeList typeList, PositionMap positionMap, ChangeTracker tracker,
                           TypeValue type, Position position) {
        this.typeList = typeList;
        this.positionMap = positionMap;
        this.tracker = tracker;
        this.type = type;
        this.position = position;
    }

    @Override
    public void execute() {
        typeList.add(type);
        positionMap.put(type.key(), position);
        tracker.markChanged(type.key());
    }

    @Override
    public void rollback() {
        typeList.remove(type);
        tracker.unmark(type.key());
    }
}

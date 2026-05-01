package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.domain.Action;

import java.util.Set;

/**
 * 선택된 타입 박스들을 캔버스에서 이동하는 Command 패턴 액션.
 *
 * <p><b>책임:</b> execute 시 지정된 타입 키 집합의 위치를 (dx, dy)만큼 이동한다.
 * rollback 시 (-dx, -dy)로 역이동하여 원래 위치로 복원한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link PositionMap} — 타입 위치 이동</li>
 * </ul></p>
 * <p><b>주의:</b> 다중 선택 시 모든 선택된 박스가 동일한 델타로 이동한다.
 * 보통 {@link PushOutOverlapAction}과 {@link ComplexAction}으로 묶여 겹침을 해소한다.</p>
 */
public class MoveBoxAction implements Action {
    private final PositionMap positionMap;
    private final Set<String> typeKeys;
    private final int dx;
    private final int dy;

    public MoveBoxAction(PositionMap positionMap, Set<String> typeKeys, int dx, int dy) {
        this.positionMap = positionMap;
        this.typeKeys = typeKeys;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        typeKeys.forEach(key -> positionMap.move(key, dx, dy));
    }

    @Override
    public void rollback() {
        typeKeys.forEach(key -> positionMap.move(key, -dx, -dy));
    }
}

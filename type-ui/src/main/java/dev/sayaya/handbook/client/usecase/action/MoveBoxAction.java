package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.handbook.client.usecase.PositionMap;

import java.util.Set;

/** 선택된 타입 박스들을 캔버스에서 이동한다. */
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

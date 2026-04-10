package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.usecase.PositionMap;

/** 타입 박스의 크기를 변경한다. */
public class ResizeBoxAction implements Action {
    private final PositionMap positionMap;
    private final String typeKey;
    private final Position before;
    private final Position after;

    public ResizeBoxAction(PositionMap positionMap, String typeKey, Position before, Position after) {
        this.positionMap = positionMap;
        this.typeKey = typeKey;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        positionMap.put(typeKey, after);
    }

    @Override
    public void rollback() {
        positionMap.put(typeKey, before);
    }
}

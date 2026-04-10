package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.ChangeTracker;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;

/** 새 타입을 캔버스에 추가한다. */
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

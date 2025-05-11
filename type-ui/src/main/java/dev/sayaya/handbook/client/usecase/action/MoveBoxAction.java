package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

class MoveBoxAction implements Action {
    private final UpdatableBox element;
    private final int originX, originY, nextX, nextY;
    private final TypeListToUpsert toUpsert;
    @AssistedInject MoveBoxAction(@Assisted UpdatableBox element, @Assisted("dx") int deltaX, @Assisted("dy") int deltaY, TypeListToUpsert toUpsert) {
        this.element = element;
        this.toUpsert = toUpsert;
        var box = element.box();
        this.originX = box.x();
        this.originY = box.y();
        this.nextX = box.x() + deltaX;
        this.nextY = box.y() + deltaY;
    }
    @Override
    public void execute() {
        element.box().x(nextX).y(nextY);
        element.update();
        toUpsert.add(element.box());
    }
    @Override
    public void rollback() {
        element.box().x(originX).y(originY);
        element.update();
        toUpsert.remove(element.box());
    }
    @AssistedFactory
    interface MoveBoxActionFactory {
        MoveBoxAction _move(UpdatableBox element, @Assisted("dx") int deltaX, @Assisted("dy") int deltaY);
        default Action move(UpdatableBox element, int deltaX, int deltaY) {
            return _move(element, deltaX, deltaY);
        }
    }
}

package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.UpdatableType;

class MoveBoxAction extends EditBoxAction {
    private final UpdatableType element;
    @AssistedInject MoveBoxAction(@Assisted UpdatableType element, @Assisted("dx") int deltaX, @Assisted("dy") int deltaY, TypeList typeList) {
        super(element.value(), element.value().toBuilder().x(element.value().x() + deltaX).y(element.value().y() + deltaY).build(), typeList);
        this.element = element;
    }
    @Override
    public void execute() {
        super.execute();
        element.update();
    }
    @Override
    public void rollback() {
        super.rollback();
        element.update();
    }
    @AssistedFactory
    interface MoveBoxActionFactory {
        MoveBoxAction _move(UpdatableType element, @Assisted("dx") int deltaX, @Assisted("dy") int deltaY);
        default Action move(UpdatableType element, int deltaX, int deltaY) {
            return _move(element, deltaX, deltaY);
        }
    }
}

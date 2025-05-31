package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.UpdatableType;

class ResizeBoxAction implements Action {
    private final int originWidth, originHeight, nextWidth, nextHeight;
    private final UpdatableType element;
    @AssistedInject ResizeBoxAction(@Assisted UpdatableType element, @Assisted("w") int width, @Assisted("h") int height) {
        this.element = element;
        this.originWidth = element.value().width();
        this.originHeight = element.value().height();
        this.nextWidth = width;
        this.nextHeight = height;
    }
    @Override
    public void execute() {
        //element.value().width(nextWidth).height(nextHeight);
        element.update();
    }
    @Override
    public void rollback() {
        //element.value().width(originWidth).height(originHeight);
        element.update();
    }
    @AssistedFactory
    interface ResizeBoxActionFactory {
        ResizeBoxAction _resize(UpdatableType element, @Assisted("w") int width, @Assisted("h") int height);
        default Action resize(UpdatableType element, int width, int height) {
            return _resize(element, width, height);
        }
    }
}

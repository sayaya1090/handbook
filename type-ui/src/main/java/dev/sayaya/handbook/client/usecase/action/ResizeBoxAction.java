package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.TypeListToUpsert;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

class ResizeBoxAction implements Action {
    private final int originWidth, originHeight, nextWidth, nextHeight;
    private final UpdatableBox element;
    private final TypeListToUpsert toUpsert;
    @AssistedInject ResizeBoxAction(@Assisted UpdatableBox element, @Assisted("w") int width, @Assisted("h") int height, TypeListToUpsert toUpsert) {
        this.element = element;
        this.originWidth = element.box().width();
        this.originHeight = element.box().height();
        this.nextWidth = width;
        this.nextHeight = height;
        this.toUpsert = toUpsert;
    }
    @Override
    public void execute() {
        element.box().width(nextWidth).height(nextHeight);
        element.update();
        toUpsert.add(element.box());
    }
    @Override
    public void rollback() {
        element.box().width(originWidth).height(originHeight);
        element.update();
        toUpsert.remove(element.box());
    }
    @AssistedFactory
    interface ResizeBoxActionFactory {
        ResizeBoxAction _resize(UpdatableBox element, @Assisted("w") int width, @Assisted("h") int height);
        default Action resize(UpdatableBox element, int width, int height) {
            return _resize(element, width, height);
        }
    }
}

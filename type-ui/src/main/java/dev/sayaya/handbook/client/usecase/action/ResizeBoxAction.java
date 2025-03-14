package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

public class ResizeBoxAction implements Action {
    private final int originWidth, originHeight, nextWidth, nextHeight;
    private final UpdatableBox element;
    public ResizeBoxAction(UpdatableBox element, int width, int height) {
        this.element = element;
        this.originWidth = element.box().width();
        this.originHeight = element.box().height();
        this.nextWidth = width;
        this.nextHeight = height;
    }
    @Override
    public void execute() {
        element.box().width(nextWidth).height(nextHeight);
        element.update();
    }
    @Override
    public void rollback() {
        element.box().height(originHeight).width(originWidth);
        element.update();
    }
}

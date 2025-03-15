package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.UpdatableBox;

class MoveBoxAction implements Action {
    private final UpdatableBox element;
    private final int originX, originY, nextX, nextY;
    MoveBoxAction(UpdatableBox element, int deltaX, int deltaY) {
        this.element = element;
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
    }
    @Override
    public void rollback() {
        element.box().x(originX).y(originY);
        element.update();
    }
}

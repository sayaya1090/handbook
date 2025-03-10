package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.interfaces.BoxElement;

public class MoveBoxAction implements Action {
    private final BoxElement element;
    private final int originX, originY, nextX, nextY;
    public MoveBoxAction(BoxElement element, int deltaX, int deltaY) {
        this.element = element;
        var box = element.toDomain();
        this.originX = box.x();
        this.originY = box.y();
        this.nextX = box.x() + deltaX;
        this.nextY = box.y() + deltaY;
    }
    @Override
    public void execute() {
        element.toDomain().x(nextX).y(nextY);
        element.paint();
    }
    @Override
    public void rollback() {
        element.toDomain().x(originX).y(originY);
        element.paint();
    }
}

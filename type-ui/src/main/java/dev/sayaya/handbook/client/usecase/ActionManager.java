package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.action.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;

@Singleton
public class ActionManager {
    private static final int MAX_STACK_SIZE = 100;
    private final LinkedList<Action> undo = new LinkedList<>();
    private final LinkedList<Action> redo = new LinkedList<>();
    private final BoxList boxList;
    private UpdatableBoxList boxElementList;
    @Inject ActionManager(BoxList boxList, UpdatableBoxListObserver boxElementList) {
        this.boxList = boxList;
        boxElementList.subscribe(list -> this.boxElementList = list);
    }
    public void addType(double x, double y) {
        var box = Box.builder().name("Untitle").x((int)x).y((int)y).width(200).height(200).build();
        var action = new ComplexAction (
                new CreateBoxAction(boxList, box),
                new PushOutOverlapAction(box, boxElementList)
        );
        push(action);
        action.execute();
    }
    public void delType(Box box) {
        var action = new DeleteBoxAction(boxList, box);
        push(action);
        action.execute();
    }
    public void move(UpdatableBox boxElement, int deltaX, int deltaY) {
        var nextBox = boxElement.box().toBuilder()
                .x(boxElement.box().x() + deltaX).y(boxElement.box().y() + deltaY)
                .build();
        var action = new ComplexAction(
                new MoveBoxAction(boxElement, deltaX, deltaY),
                new PushOutOverlapAction(nextBox, boxElementList)
        );
        push(action);
        action.execute();
    }
    private void push(Action action) {
        if (undo.size() >= MAX_STACK_SIZE) undo.removeFirst(); // 가장 오래된 작업 제거
        undo.add(action);
        redo.clear();
    }
    public synchronized void undo() {
        if(undo.isEmpty()) return;
        var last = undo.removeLast();
        last.rollback();
        redo.add(last);
    }
    public synchronized void redo() {
        if(redo.isEmpty()) return;
        var last = redo.removeLast();
        last.execute();
        undo.add(last);
    }
}

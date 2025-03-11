package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.BoxElement;
import dev.sayaya.handbook.client.usecase.action.CreateBoxAndPushOutOverlap;
import dev.sayaya.handbook.client.usecase.action.MoveBoxAction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;

@Singleton
public class ActionManager {
    private static final int MAX_STACK_SIZE = 100;
    private final LinkedList<Action> undo = new LinkedList<>();
    private final LinkedList<Action> redo = new LinkedList<>();
    private final BoxList boxList;
    private final BoxElementList boxElementList;
    @Inject ActionManager(BoxList boxList, BoxElementList boxElementList) {
        this.boxList = boxList;
        this.boxElementList = boxElementList;
    }
    public void addType(double x, double y) {
        var box = Box.builder().name("Untitle").x((int)x).y((int)y).width(100).height(100).build();
        var action = new CreateBoxAndPushOutOverlap(boxList, boxElementList, box);
        push(action);
        action.execute();
    }
    public void move(BoxElement box, int deltaX, int deltaY) {
        var action = new MoveBoxAction(box, deltaX, deltaY);
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

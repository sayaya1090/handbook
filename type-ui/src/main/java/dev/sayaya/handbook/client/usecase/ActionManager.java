package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.action.CreateBoxAndPushOutOverlap;

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
        var action = new CreateBoxAndPushOutOverlap(boxList, boxElementList, x, y);
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

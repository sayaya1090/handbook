package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/** Undo/Redo 스택을 관리하는 Command 패턴 매니저. */
@Singleton
public class ActionManager {
    private static final int MAX_STACK = 100;
    private final LinkedList<Action> undoStack = new LinkedList<>();
    private final LinkedList<Action> redoStack = new LinkedList<>();
    private final BehaviorSubject<Boolean> canUndo = behavior(false);
    private final BehaviorSubject<Boolean> canRedo = behavior(false);

    @Inject ActionManager() {}

    public void execute(Action action) {
        action.execute();
        undoStack.push(action);
        if (undoStack.size() > MAX_STACK) undoStack.removeLast();
        redoStack.clear();
        updateState();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        Action action = undoStack.pop();
        action.rollback();
        redoStack.push(action);
        updateState();
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        Action action = redoStack.pop();
        action.execute();
        undoStack.push(action);
        updateState();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
        updateState();
    }

    public Observable<Boolean> canUndo() { return canUndo.asObservable(); }
    public Observable<Boolean> canRedo() { return canRedo.asObservable(); }
    public void onCanUndo(Consumer<Boolean> consumer) { canUndo.subscribe(consumer::accept); }
    public void onCanRedo(Consumer<Boolean> consumer) { canRedo.subscribe(consumer::accept); }

    private void updateState() {
        canUndo.next(!undoStack.isEmpty());
        canRedo.next(!redoStack.isEmpty());
    }
}

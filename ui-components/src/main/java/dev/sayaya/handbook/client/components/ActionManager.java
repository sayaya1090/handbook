package dev.sayaya.handbook.client.components;

import dev.sayaya.handbook.domain.Action;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * Undo/Redo 스택을 관리하는 Command 패턴 매니저.
 *
 * <p><b>책임:</b> {@link Action}을 실행하고 undo/redo 스택에 저장.
 * canUndo/canRedo 상태를 {@link BehaviorSubject}로 발행하여 버튼 활성화를 반응형으로 제어한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 상태 관리). document-ui, type-ui의 버튼과 SaveAction이 사용한다.</p>
 *
 * <p><b>주의:</b> 스택 최대 크기 100. 새 액션 실행 시 redo 스택이 초기화된다.
 * {@link #clear()}는 Save 성공 시 호출하여 양쪽 스택을 모두 비운다.</p>
 */
@Singleton
public class ActionManager {
    private static final int MAX_STACK = 100;
    private final LinkedList<Action> undoStack = new LinkedList<>();
    private final LinkedList<Action> redoStack = new LinkedList<>();
    private final BehaviorSubject<Boolean> canUndo = behavior(false);
    private final BehaviorSubject<Boolean> canRedo = behavior(false);

    @Inject public ActionManager() {}

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

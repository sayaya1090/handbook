package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * 캔버스 편집 모드.
 * - VIEW: 조회만. 모든 편집 비활성.
 * - LAYOUT: 박스 이동/리사이즈. 더블클릭 인라인 편집 비활성.
 * - TYPE: 타입 이름/속성 인라인 편집. 드래그 이동 비활성.
 */
@Singleton
public class CanvasMode {
    public enum Mode { VIEW, LAYOUT, TYPE, READONLY }

    public interface CanvasState {
        default void onTypeMouseDown(elemental2.dom.MouseEvent e, dev.sayaya.handbook.client.interfaces.selection.DragShapeElement dragShape) {}
        default void onNameDblClick(elemental2.dom.Event e, Runnable action) {}
        default void onVersionDblClick(elemental2.dom.Event e, Runnable action) {}
        default void onResizeMouseDown(elemental2.dom.MouseEvent e, Runnable action) {}
        default void onCanvasKeyDown(elemental2.dom.KeyboardEvent e, Runnable action) {}
    }

    public static class LayoutState implements CanvasState {
        @Override
        public void onTypeMouseDown(elemental2.dom.MouseEvent e, dev.sayaya.handbook.client.interfaces.selection.DragShapeElement dragShape) {
            dragShape.show((int) e.clientX, (int) e.clientY);
        }
        @Override
        public void onResizeMouseDown(elemental2.dom.MouseEvent e, Runnable action) {
            action.run();
        }
        @Override
        public void onCanvasKeyDown(elemental2.dom.KeyboardEvent e, Runnable action) {
            action.run();
        }
    }

    public static class TypeState implements CanvasState {
        @Override
        public void onNameDblClick(elemental2.dom.Event e, Runnable action) {
            action.run();
        }
        @Override
        public void onVersionDblClick(elemental2.dom.Event e, Runnable action) {
            action.run();
        }
        @Override
        public void onCanvasKeyDown(elemental2.dom.KeyboardEvent e, Runnable action) {
            action.run();
        }
    }

    public static class ViewState implements CanvasState {}

    public static class ReadOnlyState implements CanvasState {}

    private final BehaviorSubject<Mode> subject = behavior(Mode.LAYOUT);
    private CanvasState currentState = new LayoutState();

    @Inject CanvasMode() {}

    public Observable<Mode> observable() { return subject.asObservable(); }
    public Mode getValue() { return subject.getValue(); }

    public void setMode(Mode mode) { 
        subject.next(mode);
        switch (mode) {
            case VIEW: currentState = new ViewState(); break;
            case LAYOUT: currentState = new LayoutState(); break;
            case TYPE: currentState = new TypeState(); break;
            case READONLY: currentState = new ReadOnlyState(); break;
        }
    }

    public CanvasState getCurrentState() { return currentState; }

    public void subscribe(Consumer<Mode> consumer) {
        subject.subscribe(consumer::accept);
    }
}

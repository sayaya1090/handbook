package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.action.ActionFactory;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class ActionManager {
    private static final int MAX_STACK_SIZE = 100;
    private final LinkedList<Action> undo = new LinkedList<>();
    private final LinkedList<Action> redo = new LinkedList<>();
    private final ActionFactory factory;
    private final LayoutProvider layout;
    @Delegate private final LayoutActionManager layoutActionManager;
    @Inject ActionManager(ActionFactory factory, LayoutProvider layout, LayoutActionManager layoutActionManager) {
        this.factory = factory;
        this.layout = layout;
        this.layoutActionManager = layoutActionManager;
    }
    public void add(double x, double y) {
        String id = generateUniqueString();
        var type = Type.builder().id(id).name("").version("0.0.0")
                .effectDateTime(layout.getValue()!=null?layout.getValue().effectDateTime() : new Date(0))
                .expireDateTime(layout.getValue()!=null?layout.getValue().expireDateTime() : new Date(32503680000000L))
                .attributes(List.of())
                .x((int)x).y((int)y).width(400).height(170)
                .build();
        var action = factory.complex (
                factory.createBox(type),
                factory.pushOutOverlap(type)
        );
        push(action);
        action.execute();
    }
    private static String generateUniqueString() {
        return Double.toString(Math.random()).substring(2, 7); // 랜덤 숫자 (문자열)
    }
    public void delete(UpdatableType... boxes) {
        var action = factory.deleteBox(boxes);
        push(action);
        action.execute();
    }
    public void move(int deltaX, int deltaY, UpdatableType... boxElements) {
        var updateBoxes = Arrays.stream(boxElements)
                .map(boxElement -> {
                    var box = boxElement.value();
                    return box.toBuilder().x(box.x() + deltaX).y(box.y() + deltaY).build();
                }).toArray(Type[]::new);
        var pushOutAction = factory.pushOutOverlap(updateBoxes);
        var actions = Stream.concat(
                Arrays.stream(boxElements).map(boxElement -> factory.move(boxElement, deltaX, deltaY)),
                Stream.of(pushOutAction)
        ).toArray(Action[]::new);
        var action = factory.complex(actions);
        push(action);
        action.execute();
    }
    public void edit(UpdatableType boxElement, Type after) {
        var before = boxElement.value();
        var replace = factory.editBox(before, after);
        var pushOutAction = factory.pushOutOverlap(after);
        var action = factory.complex(replace, pushOutAction);
        push(action);
        action.execute();
    }
    public void load() {
        var action = factory.load();
        push(action);
        action.execute();
    }
    public void save() {
        var action = factory.save();
        action.execute();
    }
    public void changeToAfterLayout() {
        var action = layoutActionManager.changeToAfterLayout();
        if (action != null) {
            push(action);
            action.execute();
        }
    }
    public void changeToBeforeLayout() {
        var action = layoutActionManager.changeToBeforeLayout();
        if (action != null) {
            push(action);
            action.execute();
        }

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

package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.handbook.client.usecase.action.ActionFactory;
import dev.sayaya.handbook.client.usecase.action.EditBoxAction;
import dev.sayaya.handbook.client.usecase.action.ResizeBoxAction;

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
    private final BoxTailor tailor;
    @Inject ActionManager(ActionFactory factory, BoxTailor tailor) {
        this.factory = factory;
        this.tailor = tailor;
    }
    public void addType(double x, double y) {
        var type = Type.builder().id("Untitle-" + generateUniqueString()).version("0.0.0")
                .effectDateTime(new Date(0))
                .expireDateTime(new Date(32503680000000L))
                .attributes(List.of())
                .build();
        var box = Box.builder().type(type).x((int)x).y((int)y).width(250).height(1).build();
        box.height(tailor.estimateBoxHeight(box));
        var action = factory.complex (
                factory.createBox(box),
                factory.pushOutOverlap(box)
        );
        push(action);
        action.execute();
    }
    private static String generateUniqueString() {
        return Double.toString(Math.random()).substring(2, 7); // 랜덤 숫자 (문자열)
    }
    public void delType(Box... boxes) {
        var action = factory.deleteBox(boxes);
        push(action);
        action.execute();
    }
    public void move(int deltaX, int deltaY, UpdatableBox... boxElements) {
        var updateBoxes = Arrays.stream(boxElements).map(boxElement -> boxElement.box().toBuilder()
                .x(boxElement.box().x() + deltaX).y(boxElement.box().y() + deltaY)
                .build()
        ).toArray(Box[]::new);
        var pushOutAction = factory.pushOutOverlap(updateBoxes);
        var actions = Stream.concat(
                Arrays.stream(boxElements).map(boxElement -> factory.move(boxElement, deltaX, deltaY)),
                Stream.of(pushOutAction)
        ).toArray(Action[]::new);
        var action = factory.complex(actions);
        push(action);
        action.execute();
    }
    public void resize(int width, int height, UpdatableBox... boxElements) {
        var updateBoxes = Arrays.stream(boxElements).map(boxElement -> boxElement.box().toBuilder()
                .width(width).height(height)
                .build()
        ).toArray(Box[]::new);
        var pushOutAction = factory.pushOutOverlap(updateBoxes);
        var actions = Stream.concat(
                Arrays.stream(boxElements).map(boxElement -> new ResizeBoxAction(boxElement, width, height)),
                Stream.of(pushOutAction)
        ).toArray(Action[]::new);
        var action = factory.complex(actions);
        push(action);
        action.execute();
    }
    public void title(UpdatableBox boxElement, String title) {
        var type = boxElement.box().type().toBuilder().id(title).build();
        var next = boxElement.box().toBuilder().type(type).build();
        var action = new EditBoxAction(boxElement, next);
        push(action);
        action.execute();
    }
    public void version(UpdatableBox boxElement, String version) {
        var type = boxElement.box().type().toBuilder().version(version).build();
        var next = boxElement.box().toBuilder().type(type).build();
        var action = new EditBoxAction(boxElement, next);
        push(action);
        action.execute();
    }
    public void addValue(UpdatableBox boxElement) {
        var value = Value.builder().name("property").build();
        var nextBox = boxElement.box().toBuilder().addValue(value).build();
        nextBox.height(tailor.estimateBoxHeight(nextBox));
        var add = factory.addAttribute(boxElement, value);
        var resize = new ResizeBoxAction(boxElement, nextBox.width(), nextBox.height());
        var pushOutAction = factory.pushOutOverlap(nextBox);
        var action = factory.complex(add, resize, pushOutAction);
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

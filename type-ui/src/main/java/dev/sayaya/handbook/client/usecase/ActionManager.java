package dev.sayaya.handbook.client.usecase;

import dagger.Lazy;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.handbook.client.usecase.action.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Stream;

@Singleton
public class ActionManager {
    private static final int MAX_STACK_SIZE = 100;
    private final LinkedList<Action> undo = new LinkedList<>();
    private final LinkedList<Action> redo = new LinkedList<>();
    private final BoxList boxList;
    private final Lazy<UpdatableBoxList> boxElementList;
    @Inject ActionManager(BoxList boxList, Lazy<UpdatableBoxList> boxElementList) {
        this.boxList = boxList;
        this.boxElementList = boxElementList;
    }
    public void addType(double x, double y) {
        var box = Box.builder().name("Untitle").x((int)x).y((int)y).width(300).height(1).build();
        box.height(boxElementList.get().estimateBoxHeight(box));
        var action = new ComplexAction (
                new CreateBoxAction(boxList, box),
                new PushOutOverlapAction(new Box[] { box }, boxElementList.get())
        );
        push(action);
        action.execute();
    }
    public void delType(Box... boxes) {
        var action = new DeleteBoxAction(boxList, boxes);
        push(action);
        action.execute();
    }
    public void move(int deltaX, int deltaY, UpdatableBox... boxElements) {
        var updateBoxes = Arrays.stream(boxElements).map(boxElement -> boxElement.box().toBuilder()
                .x(boxElement.box().x() + deltaX).y(boxElement.box().y() + deltaY)
                .build()
        ).toArray(Box[]::new);
        var pushOutAction = new PushOutOverlapAction(updateBoxes, boxElementList.get());
        var actions = Stream.concat(
                Arrays.stream(boxElements).map(boxElement -> new MoveBoxAction(boxElement, deltaX, deltaY)),
                Stream.of(pushOutAction)
        ).toArray(Action[]::new);
        var action = new ComplexAction(actions);
        push(action);
        action.execute();
    }
    public void resize(int width, int height, UpdatableBox... boxElements) {
        var updateBoxes = Arrays.stream(boxElements).map(boxElement -> boxElement.box().toBuilder()
                .width(width).height(height)
                .build()
        ).toArray(Box[]::new);
        var pushOutAction = new PushOutOverlapAction(updateBoxes, boxElementList.get());
        var actions = Stream.concat(
                Arrays.stream(boxElements).map(boxElement -> new ResizeBoxAction(boxElement, width, height)),
                Stream.of(pushOutAction)
        ).toArray(Action[]::new);
        var action = new ComplexAction(actions);
        push(action);
        action.execute();
    }
    public void title(UpdatableBox boxElement, String title) {
        var next = boxElement.box().toBuilder().name(title).build();
        var action = new EditBoxAction(boxElement, next);
        push(action);
        action.execute();
    }
    public void addValue(UpdatableBox boxElement) {
        var value = Value.builder().name("property").build();
        var nextBox = boxElement.box().toBuilder().addValue(value).build();
        nextBox.height(boxElementList.get().estimateBoxHeight(nextBox));
        var add = new AddAttributeAction(boxElement, value);
        var resize = new ResizeBoxAction(boxElement, nextBox.width(), nextBox.height());
        var pushOutAction = new PushOutOverlapAction(new Box[] { nextBox }, boxElementList.get());
        var action = new ComplexAction(add, resize, pushOutAction);
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

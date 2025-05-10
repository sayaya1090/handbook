package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.action.ActionFactory;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ActionManager {
    private static final int MAX_STACK_SIZE = 100;
    private final LinkedList<Action> undo = new LinkedList<>();
    private final LinkedList<Action> redo = new LinkedList<>();
    private final ActionFactory factory;
    private final BoxTailor tailor;
    private final LayoutProvider layout;
    @Delegate private final LayoutActionManager layoutActionManager;
    @Inject ActionManager(ActionFactory factory, BoxTailor tailor, LayoutProvider layout, LayoutActionManager layoutActionManager) {
        this.factory = factory;
        this.tailor = tailor;
        this.layout = layout;
        this.layoutActionManager = layoutActionManager;
    }
    public void addType(double x, double y) {
        var type = Type.builder().id("Untitle-" + generateUniqueString()).version("0.0.0")
                .effectDateTime(layout.getValue().effectDateTime())
                .expireDateTime(layout.getValue().expireDateTime())
                .attributes(List.of())
                .x((int)x).y((int)y).width(550).height(1)
                .build();
        type = type.height(tailor.estimateBoxHeight(type));
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
    public void delType(Type... boxes) {
        var action = factory.deleteBox(boxes);
        push(action);
        action.execute();
    }
    public void move(int deltaX, int deltaY, UpdatableBox... boxElements) {
        var updateBoxes = Arrays.stream(boxElements)
                .map(boxElement -> {
                    var box = boxElement.box();
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
    public void resize(int width, int height, UpdatableBox... boxElements) {
        var updateBoxes = Arrays.stream(boxElements).map(boxElement -> boxElement.box().toBuilder()
                .width(width).height(height).build()
        ).toArray(Type[]::new);
        var pushOutAction = factory.pushOutOverlap(updateBoxes);
        var actions = Stream.concat(
                Arrays.stream(boxElements).map(boxElement -> factory.resize(boxElement, width, height)),
                Stream.of(pushOutAction)
        ).toArray(Action[]::new);
        var action = factory.complex(actions);
        push(action);
        action.execute();
    }
    public void title(UpdatableBox boxElement, String title) {
        var prev = boxElement.box();
        var next = boxElement.box().toBuilder().id(title).build();
        var action = factory.replaceBox(prev, next);
        push(action);
        action.execute();
    }
    public void version(UpdatableBox boxElement, String version) {
        var prev = boxElement.box();
        var next = boxElement.box().toBuilder().version(version).build();
        var action = factory.replaceBox(prev, next);
        push(action);
        action.execute();
    }
    public void effectDateTime(UpdatableBox boxElement, Date effectDateTime) {
        var next = boxElement.box().toBuilder().effectDateTime(effectDateTime).build();
        var action = factory.editBox(boxElement.box(), next);
        push(action);
        action.execute();
    }
    public void expireDateTime(UpdatableBox boxElement, Date expireDateTime) {
        var next = boxElement.box().toBuilder().expireDateTime(expireDateTime).build();
        var action = factory.editBox(boxElement.box(), next);
        push(action);
        action.execute();
    }
    public void addValue(UpdatableBox boxElement) {
        var uniqueString = generateUniqueString();
        var value = Attribute.builder().id(boxElement.box().id() + "$$$" + boxElement.box().version() + "$$$" + uniqueString).name("prop-" + uniqueString).nullable(true).type("Value").build();
        var before = new LinkedList<>(boxElement.box().attributes());
        var after = new LinkedList<>(before);
        after.add(value);
        var add = factory.addAttribute(boxElement, before, after);

        var nextBox = boxElement.box().toBuilder().attribute(value).build();
        nextBox = nextBox.height(tailor.estimateBoxHeight(nextBox));
        var resize = factory.resize(boxElement, nextBox.width(), nextBox.height());
        var pushOutAction = factory.pushOutOverlap(nextBox);
        var action = factory.complex(add, resize, pushOutAction);
        push(action);
        action.execute();
    }
    public void removeValue(UpdatableBox boxElement, Attribute... attributes) {
        var set = Arrays.stream(attributes).collect(Collectors.toUnmodifiableSet());
        var nextAttributes = boxElement.box().attributes().stream().filter(a->!set.contains(a)).collect(Collectors.toUnmodifiableList());
        var rem = factory.addAttribute(boxElement, boxElement.box().attributes(), nextAttributes);

        var nextBox = boxElement.box().toBuilder().clearAttributes().attributes(nextAttributes).build();  // Resize를 위해
        nextBox = nextBox.height(tailor.estimateBoxHeight(nextBox));
        DomGlobal.console.log(nextAttributes);
        DomGlobal.console.log(boxElement.box().height());
        DomGlobal.console.log(nextBox.height());
        var resize = factory.resize(boxElement, nextBox.width(), nextBox.height());
        var action = factory.complex(rem, resize);
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

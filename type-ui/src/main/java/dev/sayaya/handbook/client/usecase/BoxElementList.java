package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.interfaces.BoxElement;
import dev.sayaya.handbook.client.interfaces.DragShapeElement;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class BoxElementList {
    @Delegate private final BehaviorSubject<BoxElement[]> elements = behavior(new BoxElement[0]);
    @Inject BoxElementList(BoxList boxList, BoxDisplayMode mode, DragShapeElement dragShapeElement) {
        boxList.subscribe(boxes -> {
            var next = Arrays.stream(boxes)
                    .map(box -> findOrCreate(box, mode, dragShapeElement))
                    .toArray(BoxElement[]::new);
            elements.next(next);
        });
    }
    private BoxElement findOrCreate(Box box, BoxDisplayMode mode, DragShapeElement dragShapeElement) {
        return Arrays.stream(elements.getValue())
                .filter(element -> element.toDomain().equals(box))
                .findFirst()
                .orElseGet(() -> {
                    var element = BoxElement.of(box, mode);
                    dragShapeElement.addDragAndDropHandler(element);
                    return element;
                });
    }
}

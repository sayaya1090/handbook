package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.BoxList;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import dev.sayaya.handbook.client.usecase.UpdatableBoxList;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 출력 요소 목록
@Singleton
public class BoxElementList implements UpdatableBoxList {
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
                .filter(element -> element.box().equals(box))
                .findFirst()
                .orElseGet(() -> {
                    var element = BoxElement.of(box, mode);
                    dragShapeElement.delegateDragAndDropHandler(element);
                    return element;
                });
    }
    @Override
    public UpdatableBox[] values() {
        return getValue();
    }
}

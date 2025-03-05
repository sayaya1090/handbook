package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.interfaces.BoxElement;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class BoxElementList {
    @Delegate private final BehaviorSubject<BoxElement[]> elements = behavior(new BoxElement[0]);
    @Inject BoxElementList(BoxList boxList, BoxDisplayMode mode) {
        boxList.subscribe(boxes -> {
            var next = Arrays.stream(boxes)
                    .map(box -> Arrays.stream(elements.getValue())
                            .filter(element -> element.toDomain().equals(box))
                            .findFirst()
                            .orElseGet(() -> new BoxElement(box, mode)))
                    .toArray(BoxElement[]::new);
            elements.next(next);
        });
    }
}

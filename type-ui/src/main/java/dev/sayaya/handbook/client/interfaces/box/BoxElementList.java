package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.TypeListEditing;
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
    private final BoxElementCache factory;
    @Inject BoxElementList(TypeListEditing typeListEditing, BoxElementCache factory) {
        this.factory = factory;
        typeListEditing.subscribe(boxes -> {
            var next = Arrays.stream(boxes).map(this::findOrCreate).toArray(BoxElement[]::new);
            elements.next(next);
        });
    }
    private BoxElement findOrCreate(Type box) {
        return Arrays.stream(elements.getValue())
                .filter(element -> element.box().equals(box))
                .findFirst()
                .orElseGet(() -> factory.getOrCreate(box));
    }
    @Override
    public UpdatableBox[] values() {
        return getValue();
    }

    @Override
    public int estimateBoxHeight(Type box) {
        return 100 + box.attributes().size()*57;
    }
}

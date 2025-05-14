package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.LayoutTypeList;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import dev.sayaya.handbook.client.usecase.UpdatableBoxList;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
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
    private final LayoutTypeList typeListEditing;
    @Inject BoxElementList(LayoutTypeList typeListEditing, BoxElementCache factory) {
        this.factory = factory;
        this.typeListEditing = typeListEditing;
        typeListEditing.distinctUntilChanged().subscribe(boxes -> {
            var next = boxes.stream().map(this::findOrCreate).toArray(BoxElement[]::new);
            elements.next(next);
        });
    }
    private BoxElement findOrCreate(Type box) {
        return Arrays.stream(elements.getValue())
                .filter(element -> element.box().equals(box))
                .findFirst()
                .orElseGet(() -> factory.getOrCreate(box));
    }
    public BoxElement find(String typeId) {
        var type = typeListEditing.getValue().stream().filter(t->{
            DomGlobal.console.log(t.id(), typeId);
            return t.id().equals(typeId);
        }).findAny().orElseThrow();
        return find(type);
    }
    private BoxElement find(Type box) {
        return Arrays.stream(elements.getValue())
                .filter(element -> element.box().equals(box))
                .findFirst()
                .orElseThrow();
    }
    @Override
    public UpdatableBox[] values() {
        return getValue();
    }

    @Override
    public int estimateBoxHeight(Type box) {
        return 170 + box.attributes().size()*53;
    }
}

package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.LayoutTypeList;
import dev.sayaya.handbook.client.usecase.UpdatableType;
import dev.sayaya.handbook.client.usecase.UpdatableTypeList;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 출력 요소 목록
@Singleton
public class TypeElementList implements UpdatableTypeList {
    @Delegate private final BehaviorSubject<TypeElement[]> elements = behavior(new TypeElement[0]);
    private final BoxElementCache factory;
    private final LayoutTypeList typeListEditing;
    @Inject TypeElementList(LayoutTypeList typeListEditing, BoxElementCache factory) {
        this.factory = factory;
        this.typeListEditing = typeListEditing;
        typeListEditing.distinctUntilChanged().subscribe(boxes -> {
            var next = boxes.stream().map(this::findOrCreate).toArray(TypeElement[]::new);
            elements.next(next);
        });
    }
    private TypeElement findOrCreate(Type box) {
        var e = Arrays.stream(elements.getValue())
                .filter(element -> element.value().equals(box))
                .findFirst()
                .orElseGet(() -> factory.getOrCreate(box));
        e.update(box);
        return e;
    }
    public TypeElement find(String typeId) {
        var type = typeListEditing.getValue().stream().filter(t->{
            return t.name().equals(typeId);
        }).findAny().orElseThrow();
        return find(type);
    }
    private TypeElement find(Type box) {
        return Arrays.stream(elements.getValue())
                .filter(element -> element.value().equals(box))
                .findFirst()
                .orElseThrow();
    }
    @Override
    public UpdatableType[] values() {
        return getValue();
    }
}

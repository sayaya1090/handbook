package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Arrays;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class BoxList {
    @Delegate private final BehaviorSubject<Box[]> boxes = behavior(new Box[0]);
    @Inject BoxList(SearchProvider searchProvider, TypeRepository typeRepository) {
        searchProvider.subscribe(search-> {
            var promise = typeRepository.search(search);
            promise.subscribe(page->{
                var boxes = Arrays.stream(page.content()).map(BoxList::map).toArray(Box[]::new);
                this.boxes.next(boxes);
            });
        });
    }
    private static Box map(Type type) {
        return Box.builder().type(type).id(type.id()).name(type.id())
                .width(200).height(1).x(500).y(500)
                .build();
    }
}

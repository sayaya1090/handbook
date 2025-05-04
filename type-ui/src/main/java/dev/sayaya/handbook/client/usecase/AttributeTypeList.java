package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class AttributeTypeList {
    @Delegate private final BehaviorSubject<String[]> types = behavior(new String[0]);
    private final List<String> primitiveTypes = List.of("Value", "Array", "Map", "File", "Document");
    @Inject AttributeTypeList(TypeListEditing typeListEditing) {
        typeListEditing.distinct().subscribe(boxes-> {
            var referenceTypes = Arrays.stream(boxes).filter(Objects::nonNull).map(Type::id);
            var concat = Stream.concat(primitiveTypes.stream(), referenceTypes);
            this.types.next(concat.toArray(String[]::new));
        });
    }
}

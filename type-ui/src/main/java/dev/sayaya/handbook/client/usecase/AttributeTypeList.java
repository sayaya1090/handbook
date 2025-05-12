package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class AttributeTypeList {
    private final List<String> primitiveTypes = List.of("Value", "Array", "Map", "File", "Document");
    @Delegate private final BehaviorSubject<String[]> types = behavior(primitiveTypes.stream().toArray(String[]::new));
    @Inject AttributeTypeList() {}
}

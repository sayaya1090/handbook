package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class TypeBasetimeProvider {
    private final Map<String, Date> basetimes = new HashMap<>();
    @Delegate private final BehaviorSubject<Date> subject = behavior(null);
    @Inject TypeBasetimeProvider() {

    }
}

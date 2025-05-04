package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class BasetimeProvider {
    @Delegate private final BehaviorSubject<Date> subject = behavior(null);
    @Inject BasetimeProvider(LayoutProvider layout) {
        layout.subscribe(period -> {
            if(period!=null) next(period.effectDateTime());
            else next(null);
        });
    }
}

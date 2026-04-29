package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class StateModule {
    @Provides @Singleton static BehaviorSubject<Progress> provideProgress() { return behavior(Progress.hide()); }
}

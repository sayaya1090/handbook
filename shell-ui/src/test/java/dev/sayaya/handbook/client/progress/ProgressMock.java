package dev.sayaya.handbook.client.progress;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class ProgressMock {
    @Provides @Singleton
    static BehaviorSubject<Progress> progressSubject() {
        return behavior(Progress.hide());
    }
    @Provides @Singleton
    static Observable<Progress> progressObservable(BehaviorSubject<Progress> subject) {
        return subject.asObservable();
    }
    @Provides @Singleton
    static Observer<Progress> progressObserver(BehaviorSubject<Progress> subject) {
        return subject;
    }
}

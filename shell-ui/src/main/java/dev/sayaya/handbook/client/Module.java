package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@dagger.Module
public interface Module {
    @Binds FrameContainer frameContainerProvider(ContentElement impl);

    @Provides
    @Singleton
    static BehaviorSubject<String> uriSubjectProvider() {
        return behavior(null);
    }

    @Provides
    @Singleton
    static Observable<String> uriObservableProvider(BehaviorSubject<String> subject) {
        return subject;
    }

    @Provides
    @Singleton
    static Observer<String> uriObserverProvider(BehaviorSubject<String> subject) {
        return subject;
    }

    @Provides
    @Singleton
    static BehaviorSubject<Progress> progressSubjectProvider() {
        return behavior(null);
    }

    @Provides
    @Singleton
    static Observable<Progress> progressObservableProvider(BehaviorSubject<Progress> subject) {
        return subject;
    }

    @Provides
    @Singleton
    static Observer<Progress> progressObserverProvider(BehaviorSubject<Progress> subject) {
        return subject;
    }

    @Provides
    @Singleton
    static BehaviorSubject<Render> renderSubjectProvider() {
        return behavior(null);
    }

    @Provides
    @Singleton
    static Observable<Render> renderObservableProvider(BehaviorSubject<Render> subject) {
        return subject;
    }

    @Provides
    @Singleton
    static Observer<Render> renderObserverProvider(BehaviorSubject<Render> subject) {
        return subject;
    }
}

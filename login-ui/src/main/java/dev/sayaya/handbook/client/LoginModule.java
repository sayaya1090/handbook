package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.ReplaySubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.ReplaySubject.replayWithBuffer;

@Module
public class LoginModule {
    @Provides @Singleton
    static ReplaySubject<Render> renderSubject() {
        return replayWithBuffer(Render.class, 1);
    }
    @Provides @Singleton
    static Observer<Render> renderer(ReplaySubject<Render> subject) {
        return subject;
    }
}

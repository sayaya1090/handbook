package dev.sayaya.handbook.client.create;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Progress;
import dev.sayaya.handbook.client.interfaces.LanguageRepository;
import dev.sayaya.handbook.client.usecase.LanguageProvider;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public abstract class MockModule {
    static {
        ClientWindow.progress = behavior(new Progress());
        ClientWindow.labels = behavior(null);
    }
    @Provides @Singleton static BehaviorSubject<Label> labels() { return ClientWindow.labels; }
    @Binds abstract LanguageProvider bindLanguageProvider(LanguageRepository impl);
}

package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.MutationReceiver;
import dev.sayaya.handbook.usecase.WindowMutationBridge;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class WorkspaceModule {
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static FetchApi fetchApi() { return new FetchApi() {}; }
    @Provides @Singleton static LanguageDetector languageDetector() {
        return () -> elemental2.dom.DomGlobal.navigator.language;
    }
    @Provides @Singleton static LanguagePackRepository languagePackRepository(FetchApi fetchApi) {
        return lang -> {
            dev.sayaya.rx.subject.BehaviorSubject<dev.sayaya.handbook.domain.Labels> subj = behavior(dev.sayaya.handbook.domain.Labels.empty());
            return subj.asObservable();
        };
    }
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return WindowMutationBridge.receiver();
    }
}

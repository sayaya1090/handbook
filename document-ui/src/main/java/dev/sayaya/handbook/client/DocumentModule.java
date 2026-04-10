package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.MutationReceiver;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.handbook.usecase.WindowMutationBridge;
import dev.sayaya.handbook.usecase.WindowWorkspaceEventBridge;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class DocumentModule {
    @Provides @Singleton static ViewportObserver viewport() {
        return new ViewportObserver();
    }
    @Provides @Singleton static BehaviorSubject<Progress> progress() {
        return behavior(Progress.hide());
    }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static Observer<Render> renderObserver() { return behavior(null); }
    @Provides @Singleton static Observer<String> uriObserver() { return behavior(null); }
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return WindowMutationBridge.receiver();
    }
    @Provides @Singleton static WorkspaceEventReceiver workspaceEventReceiver() {
        return WindowWorkspaceEventBridge.receiver();
    }
    @Provides @Singleton static ToastContainer toastContainer() {
        return new ToastContainer();
    }
    @Provides @Singleton static LanguageDetector languageDetector() {
        return () -> detectLanguage();
    }
    @Provides @Singleton static LanguagePackRepository languagePackRepository(dev.sayaya.handbook.usecase.FetchApi fetchApi) {
        return lang -> {
            elemental2.promise.Promise<Labels> promise = fetchApi.request("js/language." + lang + ".json")
                    .then(r -> r.ok ? r.json() : elemental2.promise.Promise.reject("HTTP " + r.status))
                    .then(obj -> elemental2.promise.Promise.resolve((Labels) obj))
                    .catch_(err -> elemental2.promise.Promise.resolve(Labels.empty()));
            return AsyncSubject.await(promise);
        };
    }

    private static native String detectLanguage() /*-{
        var stored = $wnd.localStorage.getItem('lang');
        if (stored) return stored.split('-')[0];
        var nav = $wnd.navigator.language;
        if (nav) return nav.split('-')[0];
        return 'en';
    }-*/;
}

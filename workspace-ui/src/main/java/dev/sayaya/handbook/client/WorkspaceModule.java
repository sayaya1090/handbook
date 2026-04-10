package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ToastContainer;
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

/**
 * workspace-ui 공통 싱글턴을 제공하는 Dagger 모듈.
 *
 * <p><b>책임:</b> Progress, FetchApi, ToastContainer, 다국어(LanguageDetector, LanguagePackRepository),
 * MutationReceiver 등 모듈 전반에서 사용하는 공유 인스턴스를 DI 그래프에 바인딩한다.</p>
 */
@Module
public class WorkspaceModule {
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static FetchApi fetchApi() { return new FetchApi() {}; }
    @Provides @Singleton static ToastContainer toastContainer() { return new ToastContainer(); }
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

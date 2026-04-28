package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.MutationReceiver;
import dev.sayaya.handbook.usecase.UserPreferences;
import dev.sayaya.handbook.usecase.AgentMutation;
import dev.sayaya.handbook.usecase.WorkspaceEvent;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * type-ui 공통 싱글턴을 제공하는 Dagger 모듈.
 *
 * <p><b>책임:</b> Progress, Render, URI Observer, MutationReceiver, WorkspaceEventReceiver,
 * ToastContainer, 다국어(LanguageDetector, LanguagePackRepository) 등 모듈 전반에서 사용하는
 * 공유 인스턴스를 DI 그래프에 바인딩한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link dev.sayaya.handbook.usecase.AgentMutation} — agent-bridge 기반 모듈 간 CustomEvent 수신</li>
 *   <li>{@link dev.sayaya.handbook.usecase.WorkspaceEvent} — 워크스페이스 이벤트 수신</li>
 *   <li>{@link dev.sayaya.handbook.usecase.FetchApi} — 언어팩 JSON fetch</li>
 * </ul></p>
 * <p><b>주의:</b> detectLanguage()는 Elemental2를 통해 localStorage/navigator.language를 읽는다.</p>
 */
@Module
public class TypeModule {
    @Provides @Singleton static BehaviorSubject<Progress> progress() {
        return behavior(Progress.hide());
    }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static Observer<Render> renderObserver() { return behavior(null); }
    @Provides @Singleton static Observer<String> uriObserver() { return behavior(null); }
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return AgentMutation.receiver();
    }
    @Provides @Singleton static WorkspaceEventReceiver workspaceEventReceiver() {
        return WorkspaceEvent.receiver();
    }
    @Provides @Singleton static ToastContainer toastContainer() {
        return new ToastContainer();
    }
    @Provides @Singleton static ConfirmDialog confirmDialog() {
        return new ConfirmDialog();
    }
    @Provides @Singleton static LanguageDetector languageDetector() {
        return TypeModule::detectLanguage;
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

    /**
     * 브라우저 환경에서 사용자 언어를 감지한다.
     * UserPreferences(handbook.lang) -> navigator.language -> "en" 순으로 폴백한다.
     */
    private static String detectLanguage() {
        String stored = UserPreferences.getLanguage();
        if (stored != null && !stored.isEmpty()) return stored.split("-")[0];
        String nav = DomGlobal.navigator.language;
        if (nav != null && !nav.isEmpty()) return nav.split("-")[0];
        return "en";
    }
}

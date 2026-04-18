package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.MutationReceiver;
import dev.sayaya.handbook.usecase.UserPreferences;
import dev.sayaya.handbook.usecase.WindowMutationBridge;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.AsyncSubject;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * workspace-ui 공통 싱글턴을 제공하는 Dagger 모듈.
 *
 * <p><b>책임:</b> Progress, FetchApi, ToastContainer, 다국어(LanguageDetector, LanguagePackRepository),
 * MutationReceiver 등 모듈 전반에서 사용하는 공유 인스턴스를 DI 그래프에 바인딩한다.</p>
 *
 * <p><b>주의:</b> LanguagePackRepository 는 반드시 실제 fetch 를 수행해야 한다. 빈 subject 만
 * 반환하면 LabelProvider.subscribe 가 구독은 되지만 emit 이 빈 Labels 뿐이라 UI 전체가
 * 영문 fallback(하드코딩 기본값) 으로 노출된다. TypeModule/DocumentModule 과 동일한
 * fetchApi + AsyncSubject.await 패턴을 사용한다. LanguageDetector 도 "ko-KR" 처럼 하이픈
 * 포함 값이 그대로 들어오면 `js/language.ko-KR.json` 을 요청해 404 → 영문 fallback 되므로
 * split("-")[0] 로 지역코드를 분리한다.</p>
 */
@Module
public class WorkspaceModule {
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static FetchApi fetchApi() { return new FetchApi() {}; }
    @Provides @Singleton static ToastContainer toastContainer() { return new ToastContainer(); }
    @Provides @Singleton static LanguageDetector languageDetector() {
        return WorkspaceModule::detectLanguage;
    }
    @Provides @Singleton static LanguagePackRepository languagePackRepository(FetchApi fetchApi) {
        return lang -> {
            elemental2.promise.Promise<Labels> promise = fetchApi.request("js/language." + lang + ".json")
                    .then(r -> r.ok ? r.json() : elemental2.promise.Promise.reject("HTTP " + r.status))
                    .then(obj -> elemental2.promise.Promise.resolve((Labels) obj))
                    .catch_(err -> elemental2.promise.Promise.resolve(Labels.empty()));
            return AsyncSubject.await(promise);
        };
    }
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return WindowMutationBridge.receiver();
    }

    /**
     * 브라우저 환경에서 사용자 언어를 감지한다.
     * UserPreferences(handbook.lang) -> navigator.language -> "en" 순으로 폴백한다.
     * "ko-KR" 처럼 지역 코드가 포함된 경우 언어 코드만 분리한다.
     */
    private static String detectLanguage() {
        String stored = UserPreferences.getLanguage();
        if (stored != null && !stored.isEmpty()) return stored.split("-")[0];
        String nav = DomGlobal.navigator.language;
        if (nav != null && !nav.isEmpty()) return nav.split("-")[0];
        return "en";
    }
}

package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.handbook.usecase.LabelSharing;
import dev.sayaya.handbook.usecase.ProgressSharing;
import dev.sayaya.handbook.usecase.UriSharing;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * agent-ui 독립 실행 시 shell 의 공유 상태를 window 브릿지를 통해 제공하는 Dagger 모듈.
 *
 * <p>shell-ui 가 {@code window.__handbook_*} 에 게시한 함수/데이터를 읽어
 * agent-ui 의 Dagger 그래프에 주입한다. 기존 HostSharedModule 을 대체한다.</p>
 */
@Module
public class AgentBridgeModule {
    @Provides @Singleton
    static Observer<Progress> progressObserver() {
        return Observer.next(p -> ProgressSharing.next(p));
    }

    @Provides @Singleton
    static Observer<String> uriObserver() {
        return Observer.next(url -> 0.navigate(url));
    }

    @Provides @Singleton
    static ViewportObserver viewportObserver() {
        return new ViewportObserver();
    }

    /** shell 의 window 브릿지에서 Labels 를 읽는 LanguageDetector. */
    @Provides @Singleton
    static LanguageDetector languageDetector() {
        return () -> {
            String lang = DomGlobal.navigator.language;
            return lang != null && lang.length() >= 2 ? lang.substring(0, 2) : "en";
        };
    }

    /** shell 의 window 브릿지에서 Labels 를 구독하는 LanguagePackRepository. */
    @Provides @Singleton
    static LanguagePackRepository languagePackRepository() {
        return lang -> {
            BehaviorSubject<Labels> subject = behavior(Labels.empty());
            Object snapshot = LabelSharing.snapshot();
            if (snapshot != null) subject.next(Js.cast(snapshot));
            DomGlobal.window.addEventListener(LabelSharing.EVENT_NAME, evt -> {
                CustomEvent<?> ce = Js.cast(evt);
                if (ce.detail != null) subject.next(Js.cast(ce.detail));
            });
            return subject.asObservable();
        };
    }

    @Provides @Singleton
    static Observable<Progress> progressObservable() {
        return behavior(Progress.hide()).asObservable();
    }

    @Provides @Singleton
    static Observable<String> uriObservable() {
        return behavior(DomGlobal.window.location.href).asObservable();
    }
}

package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.client.interfaces.AgentSessionImpl;
import dev.sayaya.handbook.client.interfaces.CommandRouter;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;
import dev.sayaya.handbook.client.usecase.*;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.handbook.domain.*;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static org.jboss.elemento.Elements.div;

@Module
public class AppMock {
    // ── Shell mocks ──
    @Provides @Singleton static BehaviorSubject<String> uri() { return behavior(null); }
    @Provides @Singleton static Observable<String> uriObservable(BehaviorSubject<String> s) { return s.asObservable(); }
    @Provides @Singleton static Observer<String> uriObserver(BehaviorSubject<String> s) { return s; }
    @Provides @Singleton static BehaviorSubject<Render> render() { return behavior(null); }
    @Provides @Singleton static Observable<Render> renderObservable(BehaviorSubject<Render> s) { return s.asObservable(); }
    @Provides @Singleton static Observer<Render> renderObserver(BehaviorSubject<Render> s) { return s; }
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observable<Progress> progressObservable(BehaviorSubject<Progress> s) { return s.asObservable(); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static FrameContainer frameContainer() {
        return elem -> null;
    }
    @Provides @Singleton static MenuRepository menuRepository() {
        return () -> behavior(List.of(
            Menu.builder()
                .title("Test Menu").order("A").icon("fa-circle").iconType("sharp")
                .url("test-tool")
                .tool(Tool.builder().title("test-tool").order("AA").icon("fa-circle").iconType("sharp").build())
                .build()
        ));
    }
    @Provides @Singleton static UserRepository userRepository() {
        return () -> behavior(new User());
    }
    @Provides @Singleton static ViewportObserver viewport() { return new ViewportObserver(); }
    @Provides @Singleton static LanguageDetector languageDetector() { return () -> "en"; }
    @Provides @Singleton static LanguagePackRepository languagePackRepository() {
        return lang -> behavior(Labels.empty());
    }

    @Provides @Singleton static dev.sayaya.handbook.usecase.FetchApi fetchApi() { 
        return new dev.sayaya.handbook.usecase.FetchApi() {
            @Override
            public elemental2.promise.Promise<elemental2.dom.Response> request(String url, elemental2.dom.RequestInit param) {
                return elemental2.promise.Promise.resolve(new elemental2.dom.Response());
            }
        };
    }
    @Provides @Singleton static dev.sayaya.handbook.client.components.ToastContainer toastContainer() { return new dev.sayaya.handbook.client.components.ToastContainer(); }

    // ── Agent mocks ──
    @Provides @Singleton static AgentSession agentSession() { return new AgentSessionImpl(); }
    @Provides @Singleton static CommandRouter commandRouter(AgentSession session) { return new CommandRouter(session); }
    @Provides @Singleton static AgentCommandDispatcher commandDispatcher(CommandRouter router) { return router; }
    @Provides @Singleton static AgentApiPort agentApi() {
        return new AgentApiPort() {
            @Override public void startSession(String workspace, String request) {}
            @Override public void respond(String workspace, String response) {}
            @Override public void abort(String workspace) {}
        };
    }
}

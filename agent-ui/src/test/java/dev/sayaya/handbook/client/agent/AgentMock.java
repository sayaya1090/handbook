package dev.sayaya.handbook.client.agent;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.AgentSessionImpl;
import dev.sayaya.handbook.client.interfaces.CommandRouter;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.client.usecase.AgentApiPort;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class AgentMock {
    @Provides @Singleton
    static AgentSession agentSession() {
        return new AgentSessionImpl();
    }

    @Provides @Singleton
    static CommandRouter commandRouter(AgentSession session) {
        return new CommandRouter(session);
    }

    @Provides @Singleton
    static AgentCommandDispatcher commandDispatcher(CommandRouter router) {
        return router;
    }

    @Provides @Singleton
    static Observer<Progress> progressObserver() {
        return behavior(Progress.hide());
    }

    @Provides @Singleton
    static Observer<String> uriObserver() {
        return behavior(null);
    }

    @Provides @Singleton
    static LanguageDetector languageDetector() { return () -> "en"; }

    @Provides @Singleton
    static LanguagePackRepository languagePackRepository() {
        return lang -> behavior(Labels.empty());
    }

    @Provides @Singleton
    static ViewportObserver viewportObserver() { return new ViewportObserver(); }

    @Provides @Singleton
    static AgentApiPort agentApi() {
        return new AgentApiPort() {
            @Override public void startSession(String workspace, String request) {}
            @Override public void respond(String workspace, String response) {}
            @Override public void abort(String workspace) {}
        };
    }
}

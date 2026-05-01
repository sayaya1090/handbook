package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.interfaces.api.BrowserLanguageDetector;
import dev.sayaya.handbook.interfaces.api.FetchLanguagePackRepository;
import dev.sayaya.handbook.usecase.*;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public interface DocumentModule {
    @Provides @Singleton static ViewportObserver viewport() { return new ViewportObserver(); }
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static Observer<Render> renderObserver() { return behavior(null); }
    @Provides @Singleton static Observer<String> uriObserver() { return behavior(null); }
    @Provides @Singleton static MutationReceiver mutationReceiver() { return AgentMutation.receiver(); }
    @Provides @Singleton static WorkspaceEventReceiver workspaceEventReceiver() { return WorkspaceEvent.receiver(); }
    @Provides @Singleton static ToastContainer toastContainer() { return new ToastContainer(); }
    @Provides @Singleton static ConfirmDialog confirmDialog() { return new ConfirmDialog(); }
    
    @Binds @Singleton LanguageDetector bindLanguageDetector(BrowserLanguageDetector impl);
    @Binds @Singleton LanguagePackRepository bindLanguagePackRepository(FetchLanguagePackRepository impl);
}

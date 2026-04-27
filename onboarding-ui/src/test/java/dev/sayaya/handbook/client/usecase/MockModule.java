package dev.sayaya.handbook.client.usecase;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.WorkspaceStylesheet;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.MutationReceiver;
import dev.sayaya.handbook.usecase.WindowMutationBridge;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Module
public class MockModule {
    @Provides @Singleton static BehaviorSubject<Progress> progress() { return behavior(Progress.hide()); }
    @Provides @Singleton static Observer<Progress> progressObserver(BehaviorSubject<Progress> s) { return s; }
    @Provides @Singleton static LabelProvider labelProvider() { 
        return new LabelProvider(() -> "en", lang -> behavior(Labels.empty()));
    }
    @Provides @Singleton static CreateWorkspaceMode createWorkspaceMode() { return new CreateWorkspaceMode(); }
    @Provides @Singleton static CreateWorkspaceParam createWorkspaceParam() { return new CreateWorkspaceParam(); }
    @Provides @Singleton static WorkspaceRepository workspaceRepository() {
        return new WorkspaceRepository() {
            @Override public Observable<String> create(String name, String type) { return behavior("new-ws-id").asObservable(); }
            @Override public Observable<Void> join(String code) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
            @Override public Observable<Void> delete(String id) { return BehaviorSubject.<Void>behavior(null).asObservable(); }
            @Override public Observable<String> update(String id, String name, String type) { return behavior(id).asObservable(); }
        };
    }
    @Provides @Singleton static dev.sayaya.handbook.client.components.ToastContainer toastContainer() {
        return new dev.sayaya.handbook.client.components.ToastContainer();
    }
    @Provides @Singleton static WorkspaceStylesheet workspaceStylesheet() {
        return new WorkspaceStylesheet();
    }
    @Provides @Singleton static MutationReceiver mutationReceiver() {
        return WindowMutationBridge.receiver();
    }
    @Provides @Singleton static AgentWorkspaceHandler agentWorkspaceHandler(CreateWorkspaceMode mode, CreateWorkspaceParam param, WorkspaceRepository api, MutationReceiver mutationReceiver) {
        return new AgentWorkspaceHandler(mode, param, api, mutationReceiver);
    }
}

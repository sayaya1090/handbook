package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = RedirectModule.class)
public interface Component {
    WorkspaceRepository workspaceRepository();
    HistoryManager historyManager();
    UriStore uriStore();
    WorkspaceList workspaceList();
    Observer<String> uriObserver();
}

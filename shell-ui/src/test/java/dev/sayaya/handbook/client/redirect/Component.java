package dev.sayaya.handbook.client.redirect;

import dev.sayaya.handbook.client.usecase.HistoryManager;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = RedirectModule.class)
public interface Component {
    WorkspaceRepository workspaceRepository();
    HistoryManager historyManager();
}

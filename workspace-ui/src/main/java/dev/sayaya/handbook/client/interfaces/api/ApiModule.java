package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;

@Module
public interface ApiModule {
    @Binds WorkspaceRepository bindWorkspaceRepository(WorkspaceApi impl);
}

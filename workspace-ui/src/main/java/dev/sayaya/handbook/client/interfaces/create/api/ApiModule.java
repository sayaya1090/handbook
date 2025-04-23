package dev.sayaya.handbook.client.interfaces.create.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.create.WorkspaceRepository;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds WorkspaceRepository workspaceRepositoryProvider(CreateWorkspaceApi impl);
}

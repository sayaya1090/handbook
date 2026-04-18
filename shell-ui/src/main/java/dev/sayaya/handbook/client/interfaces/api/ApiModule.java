package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;

@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds MenuRepository menuRepositoryProvider(MenuApi impl);
    @Binds UserRepository userRepositoryProvider(UserApi impl);
    @Binds WorkspaceRepository workspaceRepositoryProvider(WorkspaceApi impl);
}

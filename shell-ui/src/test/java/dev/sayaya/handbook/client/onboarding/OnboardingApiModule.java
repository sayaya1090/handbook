package dev.sayaya.handbook.client.onboarding;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.api.MenuApi;
import dev.sayaya.handbook.client.interfaces.api.UserApi;
import dev.sayaya.handbook.client.interfaces.api.WorkspaceApi;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.usecase.FetchApi;
import javax.inject.Singleton;

@Module
public interface OnboardingApiModule {
    @Provides @Singleton static FetchApi provideFetchApi() {
        return new OnboardingFetchMock();
    }
    @Binds MenuRepository menuRepo(MenuApi impl);
    @Binds UserRepository userRepo(UserApi impl);
    @Binds WorkspaceRepository workspaceRepo(WorkspaceApi impl);
}

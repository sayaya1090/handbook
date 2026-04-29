package dev.sayaya.handbook.client.onboarding;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ToastContainer;

import javax.inject.Singleton;

@Module
public class UiModule {
    @Provides @Singleton static ToastContainer provideToastContainer() { return new ToastContainer(); }
}

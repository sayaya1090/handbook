package dev.sayaya.handbook.client.onboarding;

import dagger.Component;
import dev.sayaya.handbook.client.Module;
import dev.sayaya.handbook.client.HostSharedModule;
import dev.sayaya.handbook.client.interfaces.api.I18nModule;
import javax.inject.Singleton;

@Singleton
@Component(modules = { Module.class, OnboardingApiModule.class, I18nModule.class, HostSharedModule.class })
public interface TestComponent {
    void inject(OnboardingApplication app);
}

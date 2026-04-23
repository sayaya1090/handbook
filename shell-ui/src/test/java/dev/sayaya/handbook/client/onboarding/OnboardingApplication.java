package dev.sayaya.handbook.client.onboarding;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.ShellInitializer;
import dev.sayaya.handbook.client.usecase.WorkspaceOnboardingBootstrapper;
import javax.inject.Inject;

public class OnboardingApplication implements EntryPoint {
    @Inject ShellInitializer shellInitializer;
    @Inject WorkspaceOnboardingBootstrapper onboardingBootstrapper;

    @Override
    public void onModuleLoad() {
        DaggerTestComponent.create().inject(this);
        shellInitializer.initialize();
        onboardingBootstrapper.initialize();
    }
}

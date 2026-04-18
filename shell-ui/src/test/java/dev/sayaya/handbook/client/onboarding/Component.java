package dev.sayaya.handbook.client.onboarding;

import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.WorkspaceOnboardingBootstrapper;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { OnboardingMock.class })
public interface Component {
    WorkspaceOnboardingBootstrapper bootstrapper();
    MenuSelected menuSelected();
    BehaviorSubject<User> userSubject();
}

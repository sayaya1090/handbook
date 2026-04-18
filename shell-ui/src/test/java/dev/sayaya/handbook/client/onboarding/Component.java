package dev.sayaya.handbook.client.onboarding;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.MenuSelected;
import dev.sayaya.handbook.client.usecase.WorkspaceOnboardingBootstrapper;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@dagger.Component(modules = { OnboardingMock.class })
public interface Component {
    WorkspaceOnboardingBootstrapper bootstrapper();
    MenuSelected menuSelected();
    BehaviorSubject<List<Workspace>> workspaceSubject();
}

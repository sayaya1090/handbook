package dev.sayaya.handbook.client.onboarding;

import dev.sayaya.handbook.client.usecase.UriStore;

import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@dagger.Component(modules = { OnboardingMock.class })
public interface Component {
    UriStore uri();
    BehaviorSubject<List<Workspace>> workspaceSubject();
}

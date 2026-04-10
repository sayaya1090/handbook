package dev.sayaya.handbook.client;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { AppMock.class })
public interface TestComponent {
    ShellInitializer shell();
    AgentInitializer agent();
}

package dev.sayaya.handbook.client;

import dagger.Component;
import dev.sayaya.handbook.client.interfaces.api.MockWorkspaceApi;

import javax.inject.Singleton;

@Singleton
@Component
public interface TestComponent {
    MockWorkspaceApi api();
    //LabelProvider labelProvider();
}
package dev.sayaya.handbook.client.tab;

import dev.sayaya.handbook.client.interfaces.TypeTabsElement;
import dev.sayaya.handbook.client.usecase.ClientSharedModule;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { MockModule.class, ClientSharedModule.class })
public interface Component {
    TypeTabsElement tabs();
}

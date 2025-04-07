package dev.sayaya.handbook.client.drawer;

import dev.sayaya.handbook.client.interfaces.drawer.DrawerElement;
import dev.sayaya.handbook.client.usecase.HostSharedModule;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { HostSharedModule.class, DrawerMock.class })
public interface Component {
    DrawerElement drawer();
}

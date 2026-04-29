package dev.sayaya.handbook.client.api;

import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UriStore;
import dev.sayaya.handbook.client.usecase.UserProvider;
import dev.sayaya.handbook.client.usecase.UserRepository;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ApiTestModule.class })
public interface ApiTestComponent {
    UserRepository userRepository();
    MenuRepository menuRepository();
    UserProvider userProvider();
    UriStore uri();
}

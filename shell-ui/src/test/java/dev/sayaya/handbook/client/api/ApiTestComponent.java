package dev.sayaya.handbook.client.api;

import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UserProvider;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ApiTestModule.class })
public interface ApiTestComponent {
    UserRepository userRepository();
    MenuRepository menuRepository();
    UserProvider userProvider();
    BehaviorSubject<String> uri();
}

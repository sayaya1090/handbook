package dev.sayaya.handbook.client.onboarding;

import dagger.Provides;
import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Singleton;

/**
 * UC-S21 (빈 워크스페이스 자동 온보딩) 테스트용 Dagger 모듈.
 * UserRepository 를 BehaviorSubject 로 mock 해 테스트가 런타임에 User 를 교체할 수 있게 한다.
 */
@dagger.Module
public class OnboardingMock {
    @Provides @Singleton BehaviorSubject<User> provideUserSubject() {
        return BehaviorSubject.behavior(null);
    }
    @Provides @Singleton UserRepository provideUserRepository(BehaviorSubject<User> subject) {
        return subject::asObservable;
    }

    /** JsPropertyMap 기반 User 합성 — 네이티브 JsType 필드에 workspaces 배열 주입. */
    public static User user(Workspace[] workspaces) {
        JsPropertyMap<Object> obj = JsPropertyMap.of();
        obj.set("id", "u-onboarding-test");
        obj.set("name", "Onboarding Tester");
        obj.set("workspaces", workspaces);
        return Js.cast(obj);
    }

    public static Workspace workspace(String id, String name) {
        JsPropertyMap<Object> obj = JsPropertyMap.of();
        obj.set("id", id);
        obj.set("name", name);
        return Js.cast(obj);
    }
}

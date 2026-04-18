package dev.sayaya.handbook.client.onboarding;

import dagger.Provides;
import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Singleton;
import java.util.List;

/**
 * UC-S21 (빈 워크스페이스 자동 온보딩) 테스트용 Dagger 모듈.
 *
 * <p>{@link WorkspaceRepository} 를 {@link BehaviorSubject} 로 mock 해 테스트가 런타임에
 * 워크스페이스 목록을 교체할 수 있게 한다. {@link User} 는 identity 만 담당하므로 목록 제어는
 * workspaceSubject 가 독립적으로 수행한다.</p>
 */
@dagger.Module
public class OnboardingMock {
    @Provides @Singleton BehaviorSubject<User> provideUserSubject() {
        return BehaviorSubject.behavior(null);
    }
    @Provides @Singleton UserRepository provideUserRepository(BehaviorSubject<User> subject) {
        return subject::asObservable;
    }
    @Provides @Singleton BehaviorSubject<List<Workspace>> provideWorkspaceSubject() {
        return BehaviorSubject.behavior(List.of());
    }
    @Provides @Singleton WorkspaceRepository provideWorkspaceRepository(BehaviorSubject<List<Workspace>> subject) {
        return subject::asObservable;
    }

    public static Workspace workspace(String id, String name) {
        JsPropertyMap<Object> obj = JsPropertyMap.of();
        obj.set("id", id);
        obj.set("name", name);
        return Js.cast(obj);
    }
}

package dev.sayaya.handbook.client;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.WindowRenderBridge;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

/**
 * 로그인 모듈 Dagger 바인딩.
 *
 * <p>Render observer 는 shell 의 window 브릿지를 통해 전달한다.
 * login-ui 가 독립 GWT 모듈이므로 shell 의 Dagger 그래프에 접근 불가.</p>
 */
@Module
public class LoginModule {
    @Provides @Singleton
    static Observer<Render> renderer() {
        return Observer.next(render -> WindowRenderBridge.next(render));
    }
}

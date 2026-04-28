package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.ui.LoginCommandRouter;
import dev.sayaya.handbook.client.usecase.LoginCommandDispatcher;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.RenderSharing;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

/**
 * 로그인 모듈 Dagger 바인딩.
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>Render observer → shell window 브릿지</li>
 *   <li>LoginCommandDispatcher → LoginCommandRouter</li>
 * </ul></p>
 */
@Module
public abstract class LoginModule {
    @Provides @Singleton
    static Observer<Render> renderer() {
        return Observer.next(render -> RenderSharing.next(render));
    }

    @Binds abstract LoginCommandDispatcher dispatcher(LoginCommandRouter impl);
}

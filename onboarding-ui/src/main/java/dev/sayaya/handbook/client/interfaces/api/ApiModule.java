package dev.sayaya.handbook.client.interfaces.api;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.usecase.FetchApi;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.dom.DomGlobal;

import javax.inject.Singleton;

/**
 * 온보딩 모듈의 핵심 API 연동을 담당하는 Dagger 모듈.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>{@link WorkspaceModule}: 워크스페이스 도메인 로직 및 저장소 바인딩</li>
 *   <li>{@link FetchApi}: 브라우저 기반의 Fetch API 구현 제공</li>
 *   <li>기타 서비스 인터페이스(Auth, Script, Environment) 바인딩</li>
 * </ul></p>
 */
@Module(includes = { WorkspaceModule.class })
public interface ApiModule {
    @Provides static FetchApi fetch() { return DomGlobal::fetch; }
    @Provides @Singleton static ViewportObserver provideViewportObserver() { return new ViewportObserver(); }
}

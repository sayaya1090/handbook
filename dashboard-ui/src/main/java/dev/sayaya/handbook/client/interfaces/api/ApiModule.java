package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.DashboardRepository;
import dev.sayaya.handbook.usecase.FetchApi;

/**
 * 대시보드 API 계층의 Dagger 바인딩 모듈.
 *
 * <p><b>책임:</b> FetchApi 인스턴스를 제공하고, DashboardApi를 DashboardRepository 포트에 바인딩한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link FetchApi} — 브라우저 fetch API 래퍼</li>
 *   <li>{@link DashboardApi} → {@link DashboardRepository} 구현체</li>
 * </ul></p>
 */
@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds DashboardRepository bindDashboardRepository(DashboardApi impl);
}

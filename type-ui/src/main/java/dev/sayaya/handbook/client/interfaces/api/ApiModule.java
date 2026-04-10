package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.LayoutRepository;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.handbook.usecase.FetchApi;

/**
 * API 어댑터 바인딩을 제공하는 Dagger 모듈.
 *
 * <p><b>책임:</b> 헥사고날 포트({@link TypeRepository}, {@link LayoutRepository})를
 * HTTP 어댑터({@link TypeApi}, {@link LayoutApi})로 바인딩하고,
 * {@link FetchApi} 인스턴스를 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeApi} — TypeRepository 구현체</li>
 *   <li>{@link LayoutApi} — LayoutRepository 구현체</li>
 *   <li>{@link FetchApi} — Fetch API 래퍼</li>
 * </ul></p>
 * <p><b>주의:</b> @Binds를 사용하므로 interface로 선언되어야 한다.</p>
 */
@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds TypeRepository typeRepository(TypeApi impl);
    @Binds LayoutRepository layoutRepository(LayoutApi impl);
}

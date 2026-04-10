package dev.sayaya.handbook.client.interfaces.api;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.usecase.DocumentRepository;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import dev.sayaya.handbook.usecase.FetchApi;

/**
 * API 계층의 Dagger DI 모듈.
 *
 * <p><b>책임:</b> {@link dev.sayaya.handbook.usecase.FetchApi} 인스턴스를 제공하고,
 * {@link DocumentApi}를 {@link dev.sayaya.handbook.client.usecase.DocumentRepository}로,
 * {@link TypeApi}를 {@link dev.sayaya.handbook.client.usecase.TypeRepository}로 바인딩한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link DocumentApi} — DocumentRepository 포트의 HTTP 어댑터 구현체</li>
 *   <li>{@link TypeApi} — TypeRepository 포트의 HTTP 어댑터 구현체</li>
 *   <li>{@link dev.sayaya.handbook.usecase.FetchApi} — 브라우저 Fetch API 래퍼</li>
 * </ul></p>
 *
 * <p><b>주의:</b> FetchApi는 익명 클래스로 생성되며, 기본 구현(인터페이스 디폴트 메서드)을 사용한다.</p>
 */
@Module
public interface ApiModule {
    @Provides static FetchApi fetch() { return new FetchApi() {}; }
    @Binds DocumentRepository bindDocumentRepository(DocumentApi impl);
    @Binds TypeRepository bindTypeRepository(TypeApi impl);
}

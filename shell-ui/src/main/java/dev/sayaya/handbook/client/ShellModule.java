package dev.sayaya.handbook.client;

import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.ContentElement;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;
import dev.sayaya.handbook.client.usecase.ProgressStore;
import dev.sayaya.handbook.client.usecase.RenderStore;
import dev.sayaya.handbook.client.usecase.UriStore;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

/**
 * shell-ui 모듈의 핵심 의존성 바인딩 설정.
 * 
 * <p>상태 관리용 Store 클래스들을 {@link Observable}(읽기) 및 {@link Observer}(쓰기) 
 * 인터페이스로 바인딩하여 주입함으로써 계층 간 결합도를 낮추고 모킹을 용이하게 한다.</p>
 */
@dagger.Module
public interface ShellModule {
    /** 콘텐츠 렌더링 컨테이너 바인딩 */
    @Binds FrameContainer frameContainer(ContentElement impl);

    /** URI 상태 스트림 바인딩 */
    @Provides @Singleton static UriStore uriStore() { return new UriStore(); }
    @Binds Observable<String> uriObservable(UriStore store);
    @Binds Observer<String> uriObserver(UriStore store);

    /** 프로그레스 상태 스트림 바인딩 */
    @Provides @Singleton static ProgressStore progressStore() { return new ProgressStore(); }
    @Binds Observable<Progress> progressObservable(ProgressStore store);
    @Binds Observer<Progress> progressObserver(ProgressStore store);

    /** 렌더링 상태 스트림 바인딩 */
    @Provides @Singleton static RenderStore renderStore() { return new RenderStore(); }
    @Binds Observable<Render> renderObservable(RenderStore store);
    @Binds Observer<Render> renderObserver(RenderStore store);
}

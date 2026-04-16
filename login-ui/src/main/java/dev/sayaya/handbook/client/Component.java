package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.ui.*;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

/**
 * 로그인 모듈 Dagger 컴포넌트.
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link LoginModule} — Render 브릿지, 커맨드 디스패처 바인딩</li>
 *   <li>{@link ApiModule} — OAuth API</li>
 * </ul></p>
 */
@Singleton
@dagger.Component(modules = { LoginModule.class, ApiModule.class })
public interface Component {
    ContentElement content();
    Observer<Render> renderer();
    OAuthApi api();
    LoginNotifyHandler notifyHandler();
    LoginAttentionHandler attentionHandler();
    LoginHighlightHandler highlightHandler();
    LoginProgressHandler progressHandler();
}

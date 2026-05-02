package dev.sayaya.handbook.client;

import dagger.Module;
import dev.sayaya.handbook.client.interfaces.UiModule;
import dev.sayaya.handbook.client.usecase.EventModule;
import dev.sayaya.handbook.client.usecase.StateModule;

/**
 * 어플리케이션의 핵심 비즈니스 로직 및 UI 모듈들을 조합하는 기반 모듈.
 * 이 모듈은 API 어댑터(Production/Mock)를 포함하지 않으며, 상위 모듈에서 주입받는다.
 */
@Module(includes = {
    UiModule.class,
    StateModule.class,
    EventModule.class
})
public interface DocumentModule {
}

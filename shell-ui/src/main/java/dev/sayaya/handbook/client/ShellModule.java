package dev.sayaya.handbook.client;

import dagger.Module;

/**
 * shell-ui 모듈의 핵심 의존성 바인딩 설정.
 * 
 * <p>관심사 분리에 따라, 상태를 관리하는 StateModule과 UI 컨테이너를 바인딩하는
 * UiModule로 분할하여 테스트 시 불필요한 의존성(DOM)을 로드하지 않도록 한다.</p>
 */
@Module(includes = { StateModule.class, UiModule.class })
public interface ShellModule {
}


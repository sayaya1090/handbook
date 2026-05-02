package dev.sayaya.handbook.client.interfaces;

import dagger.Module;
import dagger.Provides;
import dev.sayaya.handbook.client.components.ConfirmDialog;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.usecase.ViewportObserver;

import javax.inject.Singleton;

/**
 * 범환성 있는 UI 컴포넌트들을 제공하는 모듈.
 */
@Module
public interface UiModule {
    @Provides @Singleton static ToastContainer toastContainer() { return new ToastContainer(); }
    @Provides @Singleton static ConfirmDialog confirmDialog() { return new ConfirmDialog(); }
    @Provides @Singleton static ViewportObserver viewport() { return new ViewportObserver(); }
}

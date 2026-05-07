package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.controller.StatusHeaderElement;
import dev.sayaya.handbook.client.interfaces.editor.AttributeEditorDialog;

import javax.inject.Singleton;

/**
 * type-ui 모듈의 Dagger 루트 컴포넌트.
 *
 * <p><b>책임:</b> 캔버스, 컨트롤러, 속성 편집기, 상태 제공자 등 모듈 전체 의존성 그래프를 조립하고 진입점 객체를 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeModule} — 공통 싱글턴(Progress, MutationReceiver, Toast, 다국어 등) 제공</li>
 *   <li>{@link ApiModule} — TypeRepository, LayoutRepository HTTP 어댑터 바인딩</li>
 *   <li>{@link BoxElementModule} — TypeElement AssistedFactory 바인딩</li>
 * </ul></p>
 * <p><b>주의:</b> Application.onModuleLoad()에서 DaggerComponent를 통해 생성된다. 싱글턴 스코프.</p>
 */
@Singleton
@dagger.Component(modules = { TypeModule.class, ApiModule.class, BoxElementModule.class })
public interface Component {
    CanvasElement canvas();
    ControllerElement controller();
    StatusHeaderElement statusHeader();
    AttributeEditorDialog attributeEditor();
    dev.sayaya.handbook.client.interfaces.editor.DateCorrectionDialog dateCorrectionDialog();
    dev.sayaya.handbook.client.interfaces.editor.VersionCreationDialog versionCreationDialog();
    dev.sayaya.handbook.client.usecase.TypeToolManager typeToolManager();
    dev.sayaya.handbook.client.usecase.TypeStateProvider typeStateProvider();
    dev.sayaya.handbook.client.usecase.TypeSearchProvider typeSearchProvider();
    dev.sayaya.handbook.client.usecase.TypeEventHandler typeEventHandler();
    dev.sayaya.handbook.client.components.ToastContainer toastContainer();
    dev.sayaya.handbook.client.interfaces.api.TypeRepository typeRepository();
    dev.sayaya.handbook.client.interfaces.api.LayoutRepository layoutRepository();
    dev.sayaya.handbook.client.usecase.TypeList typeList();
    dev.sayaya.handbook.client.usecase.PositionMap positionMap();
    dev.sayaya.handbook.client.components.ChangeTracker changeTracker();
    dev.sayaya.handbook.client.components.ActionManager actionManager();
    dev.sayaya.handbook.client.usecase.LayoutProvider layoutProvider();
    dev.sayaya.handbook.client.usecase.LayoutList layoutList();
}

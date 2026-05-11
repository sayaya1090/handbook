package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ChangeTracker;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.api.LayoutRepository;
import dev.sayaya.handbook.client.interfaces.api.TypeRepository;
import dev.sayaya.handbook.client.interfaces.box.BoxElementModule;
import dev.sayaya.handbook.client.interfaces.canvas.CanvasElement;
import dev.sayaya.handbook.client.interfaces.controller.ActionDialElement;
import dev.sayaya.handbook.client.interfaces.controller.ControllerElement;
import dev.sayaya.handbook.client.interfaces.controller.SettingsDialElement;
import dev.sayaya.handbook.client.interfaces.controller.StatusHeaderElement;
import dev.sayaya.handbook.client.interfaces.editor.AttributeEditorDialog;
import dev.sayaya.handbook.client.interfaces.editor.DateCorrectionDialog;
import dev.sayaya.handbook.client.interfaces.editor.VersionCreationDialog;
import dev.sayaya.handbook.client.usecase.*;

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
    ActionDialElement actionDial();
    SettingsDialElement settingsDial();
    AttributeEditorDialog attributeEditor();
    DateCorrectionDialog dateCorrectionDialog();
    VersionCreationDialog versionCreationDialog();
    TypeToolManager typeToolManager();
    TypeList typeList();
    PositionMap positionMap();
    TypeStateProvider typeStateProvider();
    TypeSearchProvider typeSearchProvider();
    TypeEventHandler typeEventHandler();
    ToastContainer toastContainer();
    TypeRepository typeRepository();
    LayoutRepository layoutRepository();
    ActionManager actionManager();
    ChangeTracker changeTracker();
    LayoutProvider layoutProvider();
    LayoutList layoutList();
    dev.sayaya.handbook.client.usecase.PeriodRecalculationService periodRecalculationService();
    dev.sayaya.handbook.client.interfaces.controller.inspector.TypeInspectorPanel typeInspectorPanel();
    dev.sayaya.handbook.client.interfaces.controller.inspector.TypeBottomSheet typeBottomSheet();
    dev.sayaya.handbook.client.interfaces.controller.toolbar.TypeFloatingToolbar typeFloatingToolbar();
}

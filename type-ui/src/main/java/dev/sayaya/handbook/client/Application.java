package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.action.LoadAction;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.AgentSearch;
import dev.sayaya.handbook.usecase.AgentState;
import dev.sayaya.handbook.usecase.RenderSharing;
import dev.sayaya.handbook.usecase.WorkspaceEvent;
import dev.sayaya.handbook.usecase.WorkspaceEventReceiver;
import elemental2.dom.DomGlobal;

import static org.jboss.elemento.Elements.div;

/**
 * Type-UI 엔트리포인트.
 * Shell의 ModuleScriptManager가 js/type/type.nocache.js를 로딩하면 실행된다.
 * Canvas + Controller를 Render로 shell 프레임에 전달한다.
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        injectCss("/css/type-ui.css");

        // 워크스페이스 ID 구독 및 API 주입 (SSOT: 오직 브릿지만 의존)
        WorkspaceEventReceiver receiver = WorkspaceEvent.receiver();
        receiver.workspaceId().subscribe(workspaceId -> {
            if (workspaceId == null || workspaceId.isEmpty()) return;
            initializeData(component, workspaceId);
        });
        
        // 초기값 강제 확인 (구독 시 자동 발행되지 않는 상황 대비)
        String initialWsId = receiver.currentWorkspaceId();
        if (initialWsId != null && !initialWsId.isEmpty()) {
            initializeData(component, initialWsId);
        }

        // 워크스페이스 이벤트 핸들러 초기화 (실시간 협업)
        component.typeEventHandler().init();
        component.periodRecalculationService(); // Eager instantiation for reactive subscriptions
        org.jboss.elemento.Elements.body().add(component.toastContainer());

        // 에이전트 브릿지 등록: StateProvider, SearchProvider
        AgentState.register(component.typeStateProvider());
        AgentSearch.register(q -> component.typeSearchProvider().search(q));
        
        // 동적 도구 관리자 초기화
        component.typeToolManager().init();
        component.typeDataCoordinator().init();

        var container = div().css("type-container")
                .add(component.statusHeader())
                .add(component.controller())
                .add(div().css("type-canvas-wrapper")
                        .add(component.typeInspectorPanel())
                        .add(component.typeFloatingToolbar())
                        .add(component.typeBottomSheet())
                        .add(component.canvas()))
                .add(component.attributeEditor())
                .add(component.dateCorrectionDialog())
                .add(component.versionCreationDialog())
                .add(component.conflictResolutionDialog())
                .add(component.actionDial())
                .add(component.settingsDial());
        
        if (RenderSharing.isRegistered()) {
            // 쉘과 통합된 상태라면 툴바(좌측 레일) 숨김 — 쉘의 레일로 도구들이 통합됨.
            // 단, 상단바(statusHeader)는 포토샵 스타일 UX를 위해 노출 유지.
            component.controller().element().style.display = "none";
        }

        // shell FrameUpdater 에 Render 를 전달 — body 직접 append 는 body{position:fixed;inset:0}
        // + shell #content(100dvh) 뒤에 스택되어 뷰포트 밖으로 밀려나는 회귀를 유발한다.
        Render render = frame -> { frame.append(container.element()); return true; };
        RenderSharing.next(render);
    }

    private void initializeData(Component component, String workspaceId) {
        if (component.typeRepository() instanceof TypeApi) {
            ((TypeApi) component.typeRepository()).setWorkspace(workspaceId);
        }
        if (component.layoutRepository() instanceof LayoutApi) {
            ((LayoutApi) component.layoutRepository()).setWorkspace(workspaceId);
        }
        // 워크스페이스가 결정되었거나 바뀌었으므로 데이터를 로드
        new LoadAction(
                component.typeRepository(),
                component.layoutRepository(),
                component.typeList(),
                component.positionMap(),
                component.changeTracker(),
                component.actionManager(),
                component.layoutProvider(),
                component.layoutList(),
                component.typeDataCoordinator()
        ).execute();
    }

    /** 지정된 CSS 파일을 &lt;link&gt; 요소로 document.head에 추가한다. */
    private static void injectCss(String href) {
        var link = (elemental2.dom.HTMLLinkElement) elemental2.dom.DomGlobal.document.createElement("link");
        link.rel = "stylesheet";
        link.href = href;
        elemental2.dom.DomGlobal.document.head.appendChild(link);
    }
}

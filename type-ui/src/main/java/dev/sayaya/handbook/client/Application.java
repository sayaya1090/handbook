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

        // 워크스페이스 ID 구독 및 API 주입 (실시간 감시 포함)
        WorkspaceEvent.receiver().workspaceId().subscribe(workspaceId -> {
            // workspaceId가 null이거나 비어있으면 현재 URL에서 추출 시도 (새로고침 등 초기 진입 케이스)
            String wsId = (workspaceId != null && !workspaceId.isEmpty()) ? workspaceId : extractWorkspaceId(DomGlobal.window.location.pathname);
            if (wsId == null || wsId.isEmpty()) return;
            
            if (component.typeRepository() instanceof TypeApi) {
                ((TypeApi) component.typeRepository()).setWorkspace(wsId);
            }
            if (component.layoutRepository() instanceof LayoutApi) {
                ((LayoutApi) component.layoutRepository()).setWorkspace(wsId);
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
                    component.layoutList()
            ).execute();
        });

        // 워크스페이스 이벤트 핸들러 초기화 (실시간 협업)
        component.typeEventHandler().init();
        org.jboss.elemento.Elements.body().add(component.toastContainer());

        // 에이전트 브릿지 등록: StateProvider, SearchProvider
        AgentState.register(component.typeStateProvider());
        AgentSearch.register(q -> component.typeSearchProvider().search(q));
        
        // 동적 도구 관리자 초기화
        component.typeToolManager().init();

        var container = div().css("type-container")
                .add(component.statusHeader())
                .add(component.controller())
                .add(component.canvas())
                .add(component.attributeEditor())
                .add(component.dateCorrectionDialog())
                .add(component.versionCreationDialog());
        
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

    /**
     * URL에서 워크스페이스 ID를 추출한다. (WorkspaceEventListener와 동일 규약)
     * 예: "/workspaces/abc-123/types" -> "abc-123"
     */
    private static String extractWorkspaceId(String path) {
        if (path == null) return null;
        int idx = path.indexOf("/workspaces/");
        if (idx < 0) return null;
        String rest = path.substring(idx + "/workspaces/".length());
        int slashIdx = rest.indexOf('/');
        String wsId = slashIdx >= 0 ? rest.substring(0, slashIdx) : rest;
        int queryIdx = wsId.indexOf('?');
        if (queryIdx >= 0) wsId = wsId.substring(0, queryIdx);
        if ("onboarding".equals(wsId)) return null;
        return wsId.isEmpty() ? null : wsId;
    }

    /** 지정된 CSS 파일을 &lt;link&gt; 요소로 document.head에 추가한다. */
    private static void injectCss(String href) {
        var link = (elemental2.dom.HTMLLinkElement) elemental2.dom.DomGlobal.document.createElement("link");
        link.rel = "stylesheet";
        link.href = href;
        elemental2.dom.DomGlobal.document.head.appendChild(link);
    }
}

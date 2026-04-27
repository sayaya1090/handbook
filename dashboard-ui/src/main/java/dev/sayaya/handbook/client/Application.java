package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.WindowRenderBridge;

import java.util.Arrays;

/**
 * Dashboard-UI 엔트리포인트.
 * Shell의 ModuleScriptManager가 js/dashboard/dashboard.nocache.js를 로딩하면 실행된다.
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        injectCss("/css/dashboard.css");

        // URL에서 워크스페이스 ID 추출 후 API에 설정
        String wsId = extractWorkspaceId();
        component.dashboardApi().setWorkspace(wsId);

        // 대시보드 데이터 로딩
        component.dashboardRepository().fetchStats().subscribe(stats -> {
            if (stats != null) component.statsProvider().next(stats);
        });
        component.dashboardRepository().fetchQualityIssues().subscribe(issues -> {
            if (issues != null) component.qualityIssueList().next(Arrays.asList(issues));
        });
        component.dashboardRepository().fetchAgentActivity().subscribe(activities -> {
            if (activities != null) component.agentActivityList().next(Arrays.asList(activities));
        });

        // shell FrameUpdater 에 Render 를 전달 — body 직접 append 는 body{position:fixed;inset:0}
        // + shell #content(100dvh) 뒤에 스택되어 뷰포트 밖으로 밀려나는 회귀를 유발한다.
        Render render = frame -> { frame.append(component.dashboard().element()); return true; };
        WindowRenderBridge.next(render);
    }

    /**
     * 현재 URL 경로에서 워크스페이스 ID를 추출한다.
     * 예: "/workspace/abc-123/dashboard" -&gt; "abc-123"
     */
    private static String extractWorkspaceId() {
        String path = elemental2.dom.DomGlobal.window.location.pathname;
        int idx = path.indexOf("/workspace/");
        if (idx < 0) return "";
        String rest = path.substring(idx + "/workspace/".length());
        int slashIdx = rest.indexOf('/');
        String wsId = slashIdx >= 0 ? rest.substring(0, slashIdx) : rest;
        int queryIdx = wsId.indexOf('?');
        if (queryIdx >= 0) wsId = wsId.substring(0, queryIdx);
        return wsId;
    }

    /** 지정된 CSS 파일을 &lt;link&gt; 요소로 document.head에 추가한다. */
    private static void injectCss(String href) {
        var link = (elemental2.dom.HTMLLinkElement) elemental2.dom.DomGlobal.document.createElement("link");
        link.rel = "stylesheet";
        link.href = href;
        elemental2.dom.DomGlobal.document.head.appendChild(link);
    }
}

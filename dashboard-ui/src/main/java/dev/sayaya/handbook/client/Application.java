package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;

import java.util.Arrays;

import static org.jboss.elemento.Elements.body;

/**
 * Dashboard-UI 엔트리포인트.
 * Shell의 ModuleScriptManager가 js/dashboard.nocache.js를 로딩하면 실행된다.
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();
        injectCss("css/dashboard.css");

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

        body().add(component.dashboard());
    }

    private static native void injectCss(String href) /*-{
        var link = $doc.createElement('link');
        link.rel = 'stylesheet';
        link.href = href;
        $doc.head.appendChild(link);
    }-*/;
}

package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.AgentActivity;
import dev.sayaya.handbook.client.domain.QualityIssue;
import dev.sayaya.handbook.client.domain.WorkspaceStats;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.Arrays;

import static org.jboss.elemento.Elements.body;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Component component = DaggerComponent.create();

        // 테스트용 통계 데이터 설정
        WorkspaceStats stats = Js.cast(JsPropertyMap.of());
        stats.typeCount = 12;
        stats.documentCount = 1245;
        stats.userCount = 8;
        component.statsProvider().next(stats);

        // 테스트용 품질 이슈 데이터 설정
        QualityIssue issue1 = Js.cast(JsPropertyMap.of());
        issue1.type = "customer";
        issue1.serial = "C-001";
        issue1.field = "email";
        issue1.severity = "error";
        issue1.message = "필수 필드 누락";

        QualityIssue issue2 = Js.cast(JsPropertyMap.of());
        issue2.type = "order";
        issue2.serial = "O-042";
        issue2.field = "amount";
        issue2.severity = "warning";
        issue2.message = "값 범위 초과";

        component.qualityIssueList().next(Arrays.asList(issue1, issue2));

        // 테스트용 에이전트 활동 데이터 설정
        AgentActivity act1 = Js.cast(JsPropertyMap.of());
        act1.timestamp = System.currentTimeMillis() - 120000;
        act1.intent = "타입 일괄 생성";
        act1.commandCount = 3;
        act1.status = "COMPLETE";

        AgentActivity act2 = Js.cast(JsPropertyMap.of());
        act2.timestamp = System.currentTimeMillis() - 240000;
        act2.intent = "타입 캔버스 이동";
        act2.commandCount = 1;
        act2.status = "COMPLETE";

        component.agentActivityList().next(Arrays.asList(act1, act2));

        body().add(component.dashboard());
    }
}

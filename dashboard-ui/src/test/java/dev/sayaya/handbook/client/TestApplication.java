package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.*;
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

        // 테스트용 활성 실행 데이터 설정
        ExecutionStatusData exec1 = Js.cast(JsPropertyMap.of());
        exec1.executionId = "exec-001";
        exec1.intent = "타입 일괄 생성";
        exec1.currentGroup = 2;
        exec1.totalGroups = 5;
        exec1.progress = 0.4;
        exec1.status = "RUNNING";

        ExecutionStatusData exec2 = Js.cast(JsPropertyMap.of());
        exec2.executionId = "exec-002";
        exec2.intent = "문서 검증";
        exec2.currentGroup = 1;
        exec2.totalGroups = 3;
        exec2.progress = 0.33;
        exec2.status = "RUNNING";

        component.executionStatusList().next(Arrays.asList(exec1, exec2));

        // 테스트용 아티팩트 데이터 설정
        ArtifactData art1 = Js.cast(JsPropertyMap.of());
        art1.executionId = "exec-100";
        art1.summary = "고객 타입 필드 추가";
        art1.changes = new String[]{"ADD customer.phone", "SET customer.phone.label=전화번호"};
        art1.timestamp = System.currentTimeMillis() - 60000;

        ArtifactData art2 = Js.cast(JsPropertyMap.of());
        art2.executionId = "exec-101";
        art2.summary = "주문 스키마 변경";
        art2.changes = new String[]{"UPDATE order.amount"};
        art2.timestamp = System.currentTimeMillis() - 300000;

        component.artifactList().next(Arrays.asList(art1, art2));

        body().add(component.dashboard());
    }
}

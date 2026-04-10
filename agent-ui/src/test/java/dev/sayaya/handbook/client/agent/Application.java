package dev.sayaya.handbook.client.agent;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.*;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        // 핸들러 초기화 (구독 등록)
        components.highlightHandler();
        components.scrollHandler();
        components.progressHandler();
        components.navigateHandler();
        components.mutateHandler();
        components.searchVisualizationHandler();

        // 입력창에 테스트 워크스페이스 설정
        components.agentInputElement().setWorkspace("test-ws");

        // SSE 이벤트 브릿지: handbook-workspace-event에서 AGENT_COMMAND: 접두사를 추출하여 CommandRouter로 전달
        DomGlobal.window.addEventListener("handbook-workspace-event", evt -> {
            CustomEvent<?> ce = Js.cast(evt);
            Object detail = ce.detail;
            if (detail == null) return;
            String data = Js.cast(detail);
            if (data.startsWith("AGENT_COMMAND:")) {
                String json = data.substring("AGENT_COMMAND:".length());
                components.commandRouter().route(json);
            }
        });

        // 프로그레스 상태 표시용 테스트 요소
        HTMLDivElement progressDisplay = div().css("progress-container").id("test-progress").element();
        progressDisplay.style.set("display", "none");
        HTMLDivElement progressLabel = div().css("progress-label").element();
        progressDisplay.appendChild(progressLabel);
        ((BehaviorSubject<Progress>) components.progressObserver()).subscribe(p -> {
            if (p != null && p.enabled()) {
                progressDisplay.style.set("display", "flex");
                progressLabel.textContent = p.description() != null ? p.description() : "";
            } else {
                progressDisplay.style.set("display", "none");
            }
        });

        body()
            .add(progressDisplay)
            .add(createTestArea())
            .add(components.overlayElement())
            .add(components.confirmDialogElement())
            .add(components.previewPanelElement())
            .add(components.navigateHandler())
            .add(components.mutateHandler())
            .add(components.notifyHandler())
            .add(components.completeHandler())
            .add(components.artifactSummaryPanel())
            .add(components.agentInputElement())
            .add(div().style("position: fixed; top: 0; left: 0; right: 0; z-index: 9999; display: flex; flex-wrap: wrap; gap: 5px; padding: 8px 12px; background: rgba(255,255,255,0.95); border-bottom: 1px solid #e0e0e0;")
                .add(button("Highlight").id("btn-highlight")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"highlight\",\"seq\":1,\"description\":\"강조\",\"target\":\"#target-element\"}")))
                .add(button("Attention").id("btn-attention")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"attention\",\"seq\":2,\"description\":\"안내\",\"target\":\"#target-element\",\"style\":\"COACHMARK\",\"message\":\"이 영역을 확인하세요\",\"position\":\"bottom\",\"dismissable\":true}")))
                .add(button("Preview").id("btn-preview")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"preview\",\"seq\":3,\"description\":\"미리보기\",\"changes\":[\"이름 → 고객명\",\"설명 → 고객 설명\"]}")))
                .add(button("Confirm").id("btn-confirm")
                    .on(EventType.click, evt -> {
                        components.confirmDialogElement().onResponse(r -> {});
                        components.commandRouter().route(
                            "{\"type\":\"await_confirm\",\"seq\":4,\"description\":\"이대로 적용할까요?\",\"options\":[\"적용\",\"수정\",\"취소\"]}");
                    }))
                .add(button("Progress 30%").id("btn-progress")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"progress\",\"seq\":5,\"description\":\"처리 중...\",\"value\":3,\"max\":10}")))
                .add(button("Progress 100%").id("btn-progress-done")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"progress\",\"seq\":5,\"description\":\"완료!\",\"value\":10,\"max\":10}")))
                .add(button("Notify info").id("btn-notify-info")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"notify\",\"seq\":6,\"description\":\"알림\",\"level\":\"info\",\"message\":\"작업이 시작되었습니다.\"}")))
                .add(button("Notify warning").id("btn-notify")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"notify\",\"seq\":7,\"description\":\"경고\",\"level\":\"warning\",\"message\":\"3건의 문서에 권한이 부족합니다.\"}")))
                .add(button("Notify error").id("btn-notify-error")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"notify\",\"seq\":8,\"description\":\"오류\",\"level\":\"error\",\"message\":\"Gateway 연결에 실패했습니다.\"}")))
                .add(button("Scroll").id("btn-scroll")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"scroll\",\"seq\":9,\"description\":\"스크롤\",\"target\":\"#scroll-target\"}")))
                .add(button("Complete").id("btn-complete")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"complete\",\"seq\":10,\"description\":\"완료\",\"summary\":\"고객 타입에 전화번호 필드를 추가했습니다. 총 1개 타입, 1개 필드가 변경되었습니다.\"}")))
                .add(button("Navigate").id("btn-navigate")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"navigate\",\"seq\":11,\"description\":\"이동\",\"menu\":\"타입\",\"tool\":\"타입 관리\",\"url\":\"/workspace/ws-1/type\"}")))
                .add(button("Mutate").id("btn-mutate")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"mutate\",\"seq\":12,\"description\":\"변경\",\"changes\":[\"ADD field:phone:type=STRING\",\"SET field:phone:label=전화번호\"]}")))
                .add(button("Progress Group").id("btn-progress-group")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"progress\",\"seq\":13,\"description\":\"그룹 진행\",\"currentGroup\":2,\"totalGroups\":5,\"parallel\":3,\"stepCount\":4}")))
                .add(button("Search").id("btn-search")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"search\",\"seq\":15,\"description\":\"검색\",\"navigateTo\":\"/workspace/ws-1/type\",\"query\":\"customer\",\"targets\":[\"#target-element\",\"#scroll-target\"],\"summary\":\"customer 타입 2건 발견\"}")))
                .add(button("Complete Artifact").id("btn-complete-artifact")
                    .on(EventType.click, evt -> components.commandRouter().route(
                        "{\"type\":\"complete\",\"seq\":14,\"description\":\"완료\",\"summary\":\"타입 스키마 변경 완료\",\"executionId\":\"exec-001\",\"artifact\":{\"summary\":\"3개 필드를 추가했습니다\",\"changes\":[{\"type\":\"create\",\"target\":\"customer.phone\",\"description\":\"전화번호 필드 추가\"},{\"type\":\"update\",\"target\":\"customer.email\",\"description\":\"이메일 검증 규칙 변경\"},{\"type\":\"delete\",\"target\":\"customer.fax\",\"description\":\"팩스 필드 제거\"}]}}")))
            );
    }

    private HTMLDivElement createTestArea() {
        HTMLDivElement area = div().id("test-area").style("padding: 80px 20px 600px;").element();
        HTMLDivElement target = div().id("target-element")
                .style("width: 300px; height: 60px; background: #e3f2fd; border: 1px solid #90caf9; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin: 20px 0;")
                .element();
        target.textContent = "대상 요소 (highlight/attention 타겟)";
        HTMLDivElement scrollTarget = div().id("scroll-target")
                .style("margin-top: 400px; width: 300px; height: 60px; background: #fff3e0; border: 1px solid #ffcc80; border-radius: 8px; display: flex; align-items: center; justify-content: center;")
                .element();
        scrollTarget.textContent = "스크롤 타겟 (scroll 커맨드 대상)";
        area.appendChild(target);
        area.appendChild(scrollTarget);
        return area;
    }
}

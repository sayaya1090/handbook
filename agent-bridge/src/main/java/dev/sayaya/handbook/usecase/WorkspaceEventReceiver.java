package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.Observable;

/**
 * 워크스페이스 SSE 이벤트 수신 포트.
 * shell-ui가 EventSource로 수신한 도메인 이벤트를 이 인터페이스를 통해 발행하고,
 * document-ui/type-ui 등 편집 모듈이 구독하여 데이터를 갱신한다.
 *
 * <p>이벤트 형식: "EVENT_TYPE:payload_json"
 */
public interface WorkspaceEventReceiver {
    Observable<String> events();
}

package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.rx.Observable;

/**
 * 수신된 커맨드를 타입별로 분류하여 발행한다.
 * 각 커맨드 타입별 Observable을 제공하며, UI 컴포넌트가 구독한다.
 *
 * <p>커맨드 처리 책임 분담:
 * <ul>
 *   <li>navigate, highlight, scroll — Shell이 직접 처리 (URI/DOM 조작)</li>
 *   <li>attention, preview, await_confirm, progress, complete — agent-ui가 처리 (오버레이 렌더링)</li>
 *   <li>mutate, notify — Shell의 기존 API/토스트 시스템에 위임</li>
 * </ul>
 */
public interface AgentCommandDispatcher {
    /** 오버레이 요청 스트림 (attention 커맨드) */
    Observable<OverlayRequest> overlayRequests();
    /** 확인 요청 스트림 (await_confirm 커맨드) */
    Observable<ConfirmRequest> confirmRequests();
    /** 진행률 스트림 (progress 커맨드) */
    Observable<ProgressInfo> progressUpdates();
    /** 미리보기 스트림 (preview 커맨드) */
    Observable<String[]> previewRequests();
    /** 완료 요약 스트림 (complete 커맨드) */
    Observable<String> completions();
    /** 하이라이트 대상 스트림 (highlight 커맨드) */
    Observable<String> highlights();
    /** 스크롤 대상 스트림 (scroll 커맨드) */
    Observable<String> scrollTargets();
    /** navigate 커맨드 스트림 */
    Observable<NavigateInfo> navigations();
    /** mutate 커맨드 스트림 */
    Observable<String[]> mutations();
    /** notify 커맨드 스트림 */
    Observable<NotifyInfo> notifications();
}

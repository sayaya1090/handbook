package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.AwaitConfirmCommand;
import dev.sayaya.handbook.domain.HighlightCommand;
import dev.sayaya.handbook.domain.MutateCommand;
import dev.sayaya.handbook.domain.NavigateCommand;
import dev.sayaya.handbook.domain.NotifyCommand;
import dev.sayaya.handbook.domain.PreviewCommand;
import dev.sayaya.handbook.domain.ScrollCommand;
import dev.sayaya.handbook.client.domain.CompleteInfo;
import dev.sayaya.handbook.client.domain.OverlayRequest;
import dev.sayaya.handbook.client.domain.ProgressInfo;
import dev.sayaya.handbook.client.domain.SearchVisualizationRequest;
import dev.sayaya.rx.Observable;


/**
 * 수신된 에이전트 커맨드를 타입별로 분류하여 발행하는 디스패처 포트 인터페이스.
 *
 * <p><b>책임:</b> 각 커맨드 타입별 Observable을 제공하여 UI 컴포넌트가 구독할 수 있게 한다.
 * 단순 커맨드는 agent-bridge 프로토콜 타입을 직접 사용하고, 파생 로직이 필요한 커맨드는
 * agent-ui 도메인 타입으로 변환하여 발행한다.</p>
 * <p><b>의존관계:</b> <ul><li>interfaces 계층의 {@link dev.sayaya.handbook.client.interfaces.CommandRouter}가 구현한다.</li></ul></p>
 * <p><b>주의:</b> 커맨드 처리 책임 분담 — navigate/highlight/scroll은 Shell이 처리, attention/preview/await_confirm/progress/complete는 agent-ui가 처리, mutate/notify는 Shell의 기존 시스템에 위임.</p>
 */
public interface AgentCommandDispatcher {
    /** 오버레이 요청 스트림 (attention 커맨드) */
    Observable<OverlayRequest> overlayRequests();
    /** 확인 요청 스트림 (await_confirm 커맨드) */
    Observable<AwaitConfirmCommand> confirmRequests();
    /** 진행률 스트림 (progress 커맨드) */
    Observable<ProgressInfo> progressUpdates();
    /** 미리보기 스트림 (preview 커맨드) */
    Observable<PreviewCommand> previewRequests();
    /** 완료 정보 스트림 (complete 커맨드) */
    Observable<CompleteInfo> completions();
    /** 하이라이트 대상 스트림 (highlight 커맨드) */
    Observable<HighlightCommand> highlights();
    /** 스크롤 대상 스트림 (scroll 커맨드) */
    Observable<ScrollCommand> scrollTargets();
    /** navigate 커맨드 스트림 */
    Observable<NavigateCommand> navigations();
    /** mutate 커맨드 스트림 */
    Observable<MutateCommand> mutations();
    /** notify 커맨드 스트림 */
    Observable<NotifyCommand> notifications();
    /** search 시각화 커맨드 스트림 */
    Observable<SearchVisualizationRequest> searchVisualizations();
}

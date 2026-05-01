package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.AttentionCommand;
import dev.sayaya.handbook.domain.HighlightCommand;
import dev.sayaya.handbook.domain.NotifyCommand;
import dev.sayaya.handbook.domain.ProgressCommand;
import dev.sayaya.rx.Observable;

/**
 * 로그인 화면 커맨드를 타입별로 분류하여 발행하는 디스패처 포트 인터페이스.
 *
 * <p><b>책임:</b> 커맨드 타입별 Observable 을 제공하여 핸들러가 구독할 수 있게 한다.
 * 도메인 타입은 백엔드 agent-protocol 의 @JsType(isNative=true) 대응 버전.</p>
 */
public interface LoginCommandDispatcher {
    Observable<NotifyCommand> notifications();
    Observable<AttentionCommand> attentions();
    Observable<HighlightCommand> highlights();
    Observable<ProgressCommand> progressUpdates();
}

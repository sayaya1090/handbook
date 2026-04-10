package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * highlight 커맨드를 범용 HighlightEffect에 위임하는 핸들러.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 highlights를 구독하고, HighlightEffect로 CSS 선택자 대상 요소에 강조 효과를 적용한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 하이라이트 대상 스트림 구독</li>
 *   <li>{@link HighlightEffect} — DOM 요소 강조 효과</li>
 * </ul></p>
 */
@Singleton
public class HighlightHandler {
    private final dev.sayaya.handbook.client.components.HighlightEffect effect = new dev.sayaya.handbook.client.components.HighlightEffect();

    @Inject
    HighlightHandler(AgentCommandDispatcher dispatcher) {
        dispatcher.highlights().subscribe(target -> {
            if (target != null) effect.highlight(target);
        });
    }
}

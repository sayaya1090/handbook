package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * scroll 커맨드를 범용 ScrollEffect에 위임하는 핸들러.
 *
 * <p><b>책임:</b> AgentCommandDispatcher의 scrollTargets를 구독하고, ScrollEffect로 CSS 선택자 대상 요소에 부드러운 스크롤 + 도착 강조를 적용한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — 스크롤 대상 스트림 구독</li>
 *   <li>{@link ScrollEffect} — DOM 요소 스크롤 + 강조 효과</li>
 * </ul></p>
 */
@Singleton
public class ScrollHandler {
    private final dev.sayaya.handbook.client.components.ScrollEffect effect = new dev.sayaya.handbook.client.components.ScrollEffect();

    @Inject
    ScrollHandler(AgentCommandDispatcher dispatcher) {
        dispatcher.scrollTargets().subscribe(target -> {
            if (target != null) effect.scrollTo(target);
        });
    }
}

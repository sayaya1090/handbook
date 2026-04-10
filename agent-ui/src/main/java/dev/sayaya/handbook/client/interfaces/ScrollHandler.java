package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * scroll 커맨드 → 범용 ScrollEffect 위임.
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

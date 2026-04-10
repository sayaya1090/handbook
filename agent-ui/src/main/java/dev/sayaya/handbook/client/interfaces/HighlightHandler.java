package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * highlight 커맨드 → 범용 HighlightEffect 위임.
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

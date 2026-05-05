package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.SessionStateProvider;
import dev.sayaya.handbook.client.domain.SessionState;
import dev.sayaya.handbook.client.usecase.UriStore;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * EmptyWorkspaceOverlay의 가시성을 제어하는 Presenter.
 */
@Singleton
public class EmptyWorkspacePresenter {
    private final EmptyWorkspaceOverlay view;
    private final SessionStateProvider sessionState;
    private final UriStore uri;

    @Inject
    public EmptyWorkspacePresenter(EmptyWorkspaceOverlay view, SessionStateProvider sessionState, UriStore uri) {
        this.view = view;
        this.sessionState = sessionState;
        this.uri = uri;
    }

    public void initialize() {
        sessionState.subscribe(state -> update());
        uri.subscribe(path -> update());
    }

    private void update() {
        SessionState state = sessionState.getValue();
        String path = uri.getValue();
        if (state == null) return;
        if (state.kind() == dev.sayaya.handbook.domain.SessionStateKind.AUTHENTICATED && path != null && !path.startsWith("/workspaces")) {
            view.show();
        } else {
            view.hide();
        }
    }
}

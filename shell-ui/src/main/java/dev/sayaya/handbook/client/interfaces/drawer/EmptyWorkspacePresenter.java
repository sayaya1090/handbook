package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.usecase.UriStore;
import dev.sayaya.handbook.client.usecase.WorkspaceList;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * EmptyWorkspaceOverlay의 가시성을 제어하는 Presenter.
 */
@Singleton
public class EmptyWorkspacePresenter {
    private final EmptyWorkspaceOverlay view;
    private final WorkspaceList workspaceList;
    private final UriStore uri;

    @Inject
    public EmptyWorkspacePresenter(EmptyWorkspaceOverlay view, WorkspaceList workspaceList, UriStore uri) {
        this.view = view;
        this.workspaceList = workspaceList;
        this.uri = uri;
    }

    public void initialize() {
        workspaceList.subscribe(list -> update());
        uri.subscribe(path -> update());
    }

    private void update() {
        var list = workspaceList.getValue();
        String path = uri.getValue();
        if (list == null) return;
        if (list.isEmpty() && path != null && !path.equals("/workspaces")) {
            view.show();
        } else {
            view.hide();
        }
    }
}

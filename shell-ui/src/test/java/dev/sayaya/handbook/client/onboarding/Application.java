package dev.sayaya.handbook.client.onboarding;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Workspace;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLInputElement;
import jsinterop.base.Js;

import java.util.List;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        HTMLInputElement emptyCheckbox = Js.cast(DomGlobal.document.getElementById("empty-workspace"));
        HTMLInputElement uriInput = Js.cast(DomGlobal.document.getElementById("uri-input"));
        HTMLButtonElement applyBtn = Js.cast(DomGlobal.document.getElementById("apply-btn"));

        applyBtn.addEventListener("click", e -> {
            boolean empty = emptyCheckbox.checked;
            var repo = components.workspaceSubject();
            Workspace ws = Workspace.create("ws-1", "Default", "Description");
            repo.next(empty ? List.of() : List.of(ws));

            String uri = uriInput.value;
            components.uri().next(uri);
        });
    }
}

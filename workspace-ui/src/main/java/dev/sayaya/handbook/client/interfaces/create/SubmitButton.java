package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SubmitButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    SubmitButton(CreateWorkspaceMode mode, CreateWorkspaceParam param,
                 WorkspaceRepository api, LabelProvider labelProvider) {
        root = ButtonElementBuilder.button().filled().css("ws-submit").element();
        root.textContent = "Create";

        root.addEventListener("click", e -> {
            String value = param.getValue();
            if (value == null || value.trim().isEmpty()) return;
            if (mode.getValue() == Mode.CREATE) {
                api.create(value.trim(), null);
            } else {
                // JOIN: value is workspace ID
            }
        });

        param.subscribe(value -> {
            boolean disabled = (value == null || value.trim().isEmpty());
            root.toggleAttribute("disabled", disabled);
        });

        mode.subscribe(m -> updateLabel(m, labelProvider));
        labelProvider.subscribe(labels -> updateLabel(mode.getValue(), labelProvider));
    }

    private void updateLabel(Mode m, LabelProvider labelProvider) {
        // LabelProvider의 현재 값을 가져올 수 없으므로 기본값 사용
        if (m == Mode.CREATE) root.textContent = "Create";
        else root.textContent = "Request to join";
    }

    @Override
    public HTMLElement element() { return root; }
}

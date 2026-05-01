package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.UriSharing;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.*;

/**
 * 참여 중인 워크스페이스가 없을 때 표시되는 빈 상태 오버레이.
 */
@Singleton
public class EmptyWorkspaceOverlay implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    public EmptyWorkspaceOverlay(LabelProvider labelProvider) {
        var title = h(2).css("empty-title").element();
        var message = p().css("empty-message").element();
        var actionBtn = ButtonElementBuilder.button().filled()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-solid", "fa-plus"))
                .on(EventType.click, e -> UriSharing.navigate("/workspaces"))
                .element();

        root = div().css("shell-empty-overlay")
                .add(div().css("empty-content")
                        .add(div().css("empty-icon").add(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-house-chimney-crack")))
                        .add(title)
                        .add(message)
                        .add(div().css("empty-actions").add(actionBtn)))
                .element();
        root.style.set("display", "none");

        labelProvider.subscribe(labels -> {
            title.textContent = labels.getOrDefault("workspace.empty.title", "No Workspaces Found");
            message.textContent = labels.getOrDefault("workspace.empty.message", "You haven't joined any workspaces yet. Create a new one or join an existing one to get started.");
            actionBtn.textContent = labels.getOrDefault("workspace.empty.action", "Get Started");
        });
    }

    public void show() {
        root.style.set("display", "flex");
    }

    public void hide() {
        root.style.set("display", "none");
    }

    @Override
    public HTMLDivElement element() {
        return root;
    }
}

package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.usecase.IntegrityAnalysisService.ResolutionProposal;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;

/**
 * 정합성 위반 시 해결책을 제안하고 선택하게 하는 다이얼로그.
 */
@Singleton
public class ConflictResolutionDialog implements IsElement<HTMLElement> {
    private final DialogElementBuilder _this = DialogElementBuilder.dialog();
    private final HTMLElement proposalContainer;
    private Consumer<ResolutionProposal> onSelect;

    @Inject
    ConflictResolutionDialog() {
        proposalContainer = div().css("conflict-proposal-container").element();
        
        _this.attr("id", "conflict-resolution-dialog")
                .headline("Integrity Conflict Detected")
                .content(div().add(p().css("conflict-message").id("conflict-message-text"))
                        .add(proposalContainer))
                .actions(div()
                        .add(ButtonElementBuilder.button().text().text("Cancel").on(EventType.click, e -> _this.close()))
                );
    }

    public void show(String message, List<ResolutionProposal> proposals, Consumer<ResolutionProposal> onSelect) {
        this.onSelect = onSelect;
        _this.element().querySelector("#conflict-message-text").textContent = message;
        proposalContainer.innerHTML = "";
        
        for (ResolutionProposal p : proposals) {
            var btn = ButtonElementBuilder.button().outlined()
                    .css("conflict-proposal-btn")
                    .add(div().css("proposal-title").text(p.title()))
                    .add(div().css("proposal-desc").text(p.description()))
                    .on(EventType.click, e -> {
                        if (this.onSelect != null) this.onSelect.accept(p);
                        _this.close();
                    })
                    .element();
            proposalContainer.appendChild(btn);
        }
        
        _this.show();
    }

    @Override
    public HTMLElement element() { return _this.element(); }
}

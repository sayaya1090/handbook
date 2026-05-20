package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.usecase.IntegrityAnalysisService.ResolutionProposal;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.DialogElementBuilder;
import dev.sayaya.handbook.domain.Labels;
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
    private Runnable onCancel;
    private Labels currentLabels = Labels.empty();

    @Inject
    ConflictResolutionDialog(dev.sayaya.handbook.usecase.LabelProvider labelProvider) {
        proposalContainer = div().css("conflict-proposal-container").element();
        
        var cancelButton = ButtonElementBuilder.button().text().text("Cancel").on(EventType.click, e -> {
            if (this.onCancel != null) this.onCancel.run();
            _this.close();
        });

        _this.attr("id", "conflict-resolution-dialog")
                .headline("Integrity Conflict Detected")
                .content(div().add(p().css("conflict-message").id("conflict-message-text"))
                        .add(proposalContainer))
                .actions(div().add(cancelButton));

        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            _this.headline(labels.getOrDefault("type.conflict.headline", "Integrity Conflict Detected"));
            cancelButton.element().textContent = labels.getOrDefault("type.conflict.cancel", "Cancel");
        });
    }

    public void show(dev.sayaya.handbook.client.usecase.IntegrityAnalysisService.AnalysisResult result, 
                     Consumer<ResolutionProposal> onSelect, Runnable onCancel) {
        this.onSelect = onSelect;
        this.onCancel = onCancel;

        String message = currentLabels.getOrDefault("type.conflict.message", "The referenced type '{id}' is only available from {start} to {end}.")
                .replace("{id}", result.refId())
                .replace("{start}", result.coverageStart() == -1 ? "N/A" : dev.sayaya.handbook.client.usecase.DateFormatter.format(result.coverageStart()))
                .replace("{end}", result.coverageEnd() == -1 ? "N/A" : dev.sayaya.handbook.client.usecase.DateFormatter.format(result.coverageEnd()));

        _this.element().querySelector("#conflict-message-text").textContent = message;
        proposalContainer.innerHTML = "";
        
        for (ResolutionProposal p : result.proposals()) {
            String titleKey = p.type() == dev.sayaya.handbook.client.usecase.IntegrityAnalysisService.ProposalType.ADJUST_OWNER 
                    ? "type.conflict.adjust_owner.title" : "type.conflict.extend_ref.title";
            String descKey = p.type() == dev.sayaya.handbook.client.usecase.IntegrityAnalysisService.ProposalType.ADJUST_OWNER 
                    ? "type.conflict.adjust_owner.desc" : "type.conflict.extend_ref.desc";

            String title = currentLabels.getOrDefault(titleKey, "Adjust Period");
            String desc = currentLabels.getOrDefault(descKey, "Change '{id}' to [{start} ~ {end}].")
                    .replace("{id}", p.targetId())
                    .replace("{start}", dev.sayaya.handbook.client.usecase.DateFormatter.format(p.newStart()))
                    .replace("{end}", dev.sayaya.handbook.client.usecase.DateFormatter.format(p.newEnd()));

            var card = div().css("type-card", "conflict-proposal-card")
                    .add(div().css("conflict-proposal-content")
                            .add(org.jboss.elemento.Elements.h(4, title).css("conflict-proposal-title"))
                            .add(p().css("conflict-proposal-desc").text(desc)))
                    .add(div().css("conflict-proposal-actions")
                            .add(ButtonElementBuilder.button().filled().text(currentLabels.getOrDefault("type.conflict.apply", "Apply"))
                                    .on(EventType.click, e -> {
                                        if (this.onSelect != null) this.onSelect.accept(p);
                                        _this.close();
                                    })))
                    .element();
            proposalContainer.appendChild(card);
        }
        
        _this.show();
    }

    @Override
    public HTMLElement element() { return _this.element(); }
}

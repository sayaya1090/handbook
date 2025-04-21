package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceParam;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceState;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.dom.MdButtonElement;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;

import javax.inject.Inject;

import static dev.sayaya.handbook.client.domain.Label.findLabelOrDefault;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static org.jboss.elemento.Elements.div;

public class SubmitButton implements ButtonElementBuilder<MdButtonElement.MdFilledButtonElement, ButtonElementBuilder.FilledButtonElementBuilder> {
    private final HTMLContainerBuilder<HTMLDivElement> submitLabel = div();
    @Delegate private final ButtonElementBuilder.FilledButtonElementBuilder submit = button().filled().add(submitLabel).style("margin-top: 2rem;");
    private Label label;
    @Inject SubmitButton(Observable<Label> labels, CreateWorkspaceMode mode, CreateWorkspaceParam param, WorkspaceRepository api) {
        labels.subscribe(label -> update(label, mode.getValue()));
        mode.subscribe(state -> update(label, state));
        param.subscribe(value -> submit.element().disabled = (value==null || value.isEmpty()));
        submit.onClick(evt-> {
            if(mode.getValue() == CreateWorkspaceState.CREATE) api.create(param.getValue());
            else if(mode.getValue() == CreateWorkspaceState.JOIN) api.join(param.getValue());
        });
    }
    private void update(Label label, CreateWorkspaceState state) {
        this.label = label;
        if(state == CreateWorkspaceState.CREATE) submitLabel.element().innerHTML = findLabelOrDefault(label, "Create");
        else if(state == CreateWorkspaceState.JOIN) submitLabel.element().innerHTML = findLabelOrDefault(label, "Request to join");
    }
}

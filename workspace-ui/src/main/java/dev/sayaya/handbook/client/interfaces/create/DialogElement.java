package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.usecase.create.CreateWorkspaceState;
import dev.sayaya.rx.Observable;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.Label.findLabelOrDefault;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

@Singleton
class DialogElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div = div().css("dialog");
    private final HTMLContainerBuilder<HTMLLabelElement> lblOr = label().style("font-family: var(--md-sys-typescale-headline-large-font);");
    private final SectionElement selectCreateWorkspace;
    private final SectionElement selectJoinWorkspace;
    @Inject DialogElement(SelectionElementFactory factory, Observable<Label> labels, SubmitButton submit) {
        selectCreateWorkspace = factory.create(CreateWorkspaceState.CREATE);
        selectJoinWorkspace = factory.create(CreateWorkspaceState.JOIN);
        div.add(selectCreateWorkspace)
           .add(lblOr.css("divider"))
           .add(selectJoinWorkspace)
           .add(submit);
        labels.subscribe(this::update);
    }
    private void update(Label label) {
        DomGlobal.console.log(label);
        selectCreateWorkspace.label(findLabelOrDefault(label,"Create a new workspace."))
                             .supportingText(findLabelOrDefault(label,"New workspace name"));
        lblOr.element().innerHTML = findLabelOrDefault(label,"or");
        selectJoinWorkspace.label(
                        findLabelOrDefault(label,"Join an existing workspace.") + " " +
                        findLabelOrDefault(label,"Please contact the workspace administrator for the workspace ID.")
                ).supportingText(findLabelOrDefault(label,"Workspace ID to join"));
    }
}

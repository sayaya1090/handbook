package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.WorkspaceApi;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;

public class InfoTabElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder nameInput;
    private final TextFieldElementBuilder.OutlinedTextFieldElementBuilder descriptionInput;
    private final WorkspaceApi api;
    private String workspaceId;

    @Inject
    public InfoTabElement(WorkspaceApi api, LabelProvider labelProvider) {
        this.api = api;
        
        nameInput = TextFieldElementBuilder.textField().outlined().label("Workspace Name").css("mgmt-input");
        descriptionInput = TextFieldElementBuilder.textField().outlined().label("Description").css("mgmt-input");
        
        HTMLElement saveBtn = ButtonElementBuilder.button().filled()
                .text("Save Changes")
                .on(EventType.click, e -> save())
                .element();
        
        HTMLElement deleteBtn = ButtonElementBuilder.button().outlined()
                .text("Delete Workspace")
                .css("mgmt-delete-btn")
                .on(EventType.click, e -> delete())
                .element();

        root = div().css("mgmt-tab-content")
                .add(div().css("mgmt-section")
                        .add(h(3).css("mgmt-section-title").text("General Settings"))
                        .add(nameInput)
                        .add(descriptionInput)
                        .add(div().css("mgmt-actions").add(saveBtn)))
                .add(div().css("mgmt-section", "mgmt-danger-zone")
                        .add(h(3).css("mgmt-section-title").text("Danger Zone"))
                        .add(div().text("Once you delete a workspace, there is no going back. Please be certain."))
                        .add(div().css("mgmt-actions").add(deleteBtn)))
                .element();
    }

    public void setWorkspaceId(String id) {
        this.workspaceId = id;
    }

    private void save() {
        api.update(workspaceId, nameInput.element().value, descriptionInput.element().value).subscribe(ws -> {
        });
    }

    private void delete() {
    }

    @Override
    public HTMLDivElement element() { return root; }
}

package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.WorkspaceApi;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.ToastLevel;
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
    private final ToastContainer toast;
    private final LabelProvider labelProvider;
    private Labels labels = Labels.empty();
    private String workspaceId;

    @Inject
    public InfoTabElement(WorkspaceApi api, LabelProvider labelProvider, ToastContainer toast) {
        this.api = api;
        this.toast = toast;
        this.labelProvider = labelProvider;
        
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

        labelProvider.subscribe(l -> {
            this.labels = l;
            nameInput.label(l.getOrDefault("workspace.mgmt.name", "Workspace Name"));
            descriptionInput.label(l.getOrDefault("workspace.mgmt.description", "Description"));
            saveBtn.textContent = l.getOrDefault("workspace.mgmt.save", "Save Changes");
            deleteBtn.textContent = l.getOrDefault("workspace.mgmt.delete", "Delete Workspace");
        });
    }

    public void setWorkspaceId(String id) {
        this.workspaceId = id;
    }

    private void save() {
        api.update(workspaceId, nameInput.element().value, descriptionInput.element().value).subscribe(ws -> {
            toast.show(ToastLevel.SUCCESS, labels.getOrDefault("toast.workspace.updated", "Workspace updated"));
        });
    }

    private void delete() {
        api.delete(workspaceId).subscribe(v -> {
            toast.show(ToastLevel.SUCCESS, labels.getOrDefault("toast.workspace.deleted", "Workspace deleted"));
        });
    }

    @Override
    public HTMLDivElement element() { return root; }
}

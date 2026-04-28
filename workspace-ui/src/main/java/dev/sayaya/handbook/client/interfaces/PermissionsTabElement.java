package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.usecase.WorkspaceApi;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;

public class PermissionsTabElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLDivElement groupList;
    private final HTMLDivElement roleList;
    private final WorkspaceApi api;
    private String workspaceId;
    private String selectedGroupId;

    @Inject
    public PermissionsTabElement(WorkspaceApi api, LabelProvider labelProvider) {
        this.api = api;
        
        groupList = div().css("mgmt-list", "mgmt-group-list").element();
        roleList = div().css("mgmt-list", "mgmt-role-list").element();

        HTMLElement assignRoleBtn = ButtonElementBuilder.button().filled()
                .text("Assign Role")
                .on(EventType.click, e -> assignRole())
                .element();

        root = div().css("mgmt-tab-content", "mgmt-dual-panel")
                .add(div().css("mgmt-panel", "mgmt-left-panel")
                        .add(h(3).css("mgmt-section-title").text("Groups"))
                        .add(groupList))
                .add(div().css("mgmt-panel", "mgmt-right-panel")
                        .add(h(3).css("mgmt-section-title").text("Assigned Roles"))
                        .add(roleList)
                        .add(div().css("mgmt-actions").add(assignRoleBtn)))
                .element();
    }

    public void setWorkspaceId(String id) {
        this.workspaceId = id;
        refreshGroups();
    }

    private void refreshGroups() {
        groupList.innerHTML = "";
        api.listGroups(workspaceId).subscribe(groups -> {
            for (Group group : groups) {
                groupList.append(createGroupItem(group));
            }
        });
    }

    private HTMLElement createGroupItem(Group group) {
        var item = div().css("mgmt-list-item").text(group.name()).element();
        if (group.id().equals(selectedGroupId)) item.classList.add("mgmt-list-item-selected");
        item.addEventListener(EventType.click.name, e -> {
            selectedGroupId = group.id();
            refreshGroups();
            refreshRoles();
        });
        return item;
    }

    private void refreshRoles() {
        roleList.innerHTML = "";
        if (selectedGroupId == null) return;
        api.listRoles(workspaceId, selectedGroupId).subscribe(roles -> {
            for (String role : roles) {
                roleList.append(div().css("mgmt-list-item").text(role).element());
            }
        });
    }

    private void assignRole() {
    }

    @Override
    public HTMLDivElement element() { return root; }
}

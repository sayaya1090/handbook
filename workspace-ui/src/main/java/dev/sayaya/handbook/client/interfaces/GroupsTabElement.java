package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.client.usecase.WorkspaceApi;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;

public class GroupsTabElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLDivElement groupList;
    private final HTMLDivElement memberList;
    private final WorkspaceApi api;
    private final ToastContainer toast;
    private final LabelProvider labelProvider;
    private Labels labels = Labels.empty();
    private String workspaceId;
    private String selectedGroupId;

    @Inject
    public GroupsTabElement(WorkspaceApi api, LabelProvider labelProvider, ToastContainer toast) {
        this.api = api;
        this.toast = toast;
        this.labelProvider = labelProvider;
        
        groupList = div().css("mgmt-list", "mgmt-group-list").element();
        memberList = div().css("mgmt-list", "mgmt-member-list").element();

        HTMLElement addGroupBtn = ButtonElementBuilder.button().filled()
                .text("Add Group")
                .on(EventType.click, e -> addGroup())
                .element();

        HTMLElement addMemberBtn = ButtonElementBuilder.button().outlined()
                .text("Add Member")
                .on(EventType.click, e -> addMember())
                .element();

        root = div().css("mgmt-tab-content", "mgmt-dual-panel")
                .add(div().css("mgmt-panel", "mgmt-left-panel")
                        .add(h(3).css("mgmt-section-title").text("Groups"))
                        .add(groupList)
                        .add(div().css("mgmt-actions").add(addGroupBtn)))
                .add(div().css("mgmt-panel", "mgmt-right-panel")
                        .add(h(3).css("mgmt-section-title").text("Members"))
                        .add(memberList)
                        .add(div().css("mgmt-actions").add(addMemberBtn)))
                .element();

        labelProvider.subscribe(l -> {
            this.labels = l;
            addGroupBtn.textContent = l.getOrDefault("workspace.mgmt.groups.add", "Add Group");
            addMemberBtn.textContent = l.getOrDefault("workspace.mgmt.members.add", "Add Member");
        });
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
            refreshMembers();
        });
        return item;
    }

    private void refreshMembers() {
        memberList.innerHTML = "";
        if (selectedGroupId == null) return;
        api.listMembers(workspaceId, selectedGroupId).subscribe(users -> {
            for (User user : users) {
                memberList.append(div().css("mgmt-list-item").text(user.name()).element());
            }
        });
    }

    private void addGroup() {
        // Implementation for adding group (e.g. show dialog)
        // toast.show(ToastLevel.SUCCESS, labels.getOrDefault("toast.group.created", "Group created"));
    }

    private void addMember() {
        // Implementation for adding member
        // toast.show(ToastLevel.SUCCESS, labels.getOrDefault("toast.member.added", "Member added"));
    }

    @Override
    public HTMLDivElement element() { return root; }
}

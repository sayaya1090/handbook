package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.handbook.usecase.WindowRenderBridge;
import dev.sayaya.handbook.usecase.WindowWorkspaceEventBridge;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class Application implements EntryPoint {
    private String currentTab = "info";
    private String workspaceId;
    private WorkspaceComponent component;
    private HTMLElement contentArea;

    @Override
    public void onModuleLoad() {
        DomGlobal.console.log("!!! onModuleLoad !!!");
        try {
            this.component = DaggerWorkspaceComponent.create();
            
            // 테스트 환경에서 URL 에 ID 가 없어도 기본 렌더링 시도
            this.workspaceId = extractWorkspaceId(DomGlobal.window.location.pathname);
            if (this.workspaceId == null) this.workspaceId = "test-workspace-id";
            
            render();

            WindowWorkspaceEventBridge.receiver().workspaceId().subscribe(id -> {
                if (id != null && !id.equals(this.workspaceId)) {
                    this.workspaceId = id;
                    render();
                }
            });
        } catch (Throwable e) {
            DomGlobal.console.error("!!! FATAL: " + e.getMessage());
        }
    }

    private void render() {
        try {
            var container = div().css("workspace-mgmt-container")
                    .add(div().css("mgmt-tabs")
                            .add(createTab("info", "General"))
                            .add(createTab("groups", "Groups & Members"))
                            .add(createTab("permissions", "Roles & Permissions")))
                    .add(contentArea = div().css("mgmt-content").element());
            
            updateContent();

            Render render = frame -> {
                frame.innerHTML = "";
                frame.append(container.element());
                return true;
            };
            
            if (WindowRenderBridge.isRegistered()) {
                WindowRenderBridge.next(render);
            } else {
                body().add(container);
            }
        } catch (Throwable e) {
            DomGlobal.console.error("!!! RENDER FATAL: " + e.getMessage());
        }
    }

    private HTMLElement createTab(String id, String label) {
        var tab = div().css("mgmt-tab").text(label).element();
        if (currentTab.equals(id)) tab.classList.add("mgmt-tab-active");
        tab.addEventListener(EventType.click.name, e -> {
            currentTab = id;
            render();
        });
        return tab;
    }

    private void updateContent() {
        contentArea.innerHTML = "";
        switch (currentTab) {
            case "info":
                component.infoTab().setWorkspaceId(workspaceId);
                contentArea.append(component.infoTab().element());
                break;
            case "groups":
                component.groupsTab().setWorkspaceId(workspaceId);
                contentArea.append(component.groupsTab().element());
                break;
            case "permissions":
                component.permissionsTab().setWorkspaceId(workspaceId);
                contentArea.append(component.permissionsTab().element());
                break;
        }
    }

    private static String extractWorkspaceId(String path) {
        if (path == null) return null;
        int idx = path.indexOf("/workspaces/");
        if (idx < 0) return null;
        String rest = path.substring(idx + "/workspaces/".length());
        int slashIdx = rest.indexOf('/');
        String wsId = slashIdx >= 0 ? rest.substring(0, slashIdx) : rest;
        return wsId.isEmpty() ? null : wsId;
    }
}

package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.client.usecase.DrawerMode;
import dev.sayaya.handbook.client.usecase.UserProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.script;

@Singleton
public class WorkspaceRequireHandler {
    private final UserProvider user;
    private final DrawerMode drawerMode;
    @Inject WorkspaceRequireHandler(UserProvider user, DrawerMode drawerMode) {
        this.user = user;
        this.drawerMode = drawerMode;
    }
    public void initialize() {
        user.subscribe(this::update);
    }
    private void update(User user) {
        if(user!=null && user.workspaces()!=null && user.workspaces().length > 0) drawerMode.next(DrawerState.COLLAPSE);
        else {
            var existingScript = document.getElementById("module-script");
            if (existingScript != null) existingScript.remove();
            var script = script().attr("type", "text/javascript").id("module-script").attr("async", "true");
            script.element().src = "js/workspaceCreate.nocache.js";
            document.head.append(script.element());
            drawerMode.next(DrawerState.HIDE);
        }
    }
}

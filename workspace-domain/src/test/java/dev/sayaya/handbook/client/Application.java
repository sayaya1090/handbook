package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Workspace;
import elemental2.dom.DomGlobal;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        testWorkspaceBuilder();
        testUserBuilder();
        testGroupBuilder();
        DomGlobal.console.log("WORKSPACE_DOMAIN_TEST_READY");
    }

    private void testWorkspaceBuilder() {
        Workspace ws = Workspace.builder().id("ws-1").name("My Workspace").build();
        DomGlobal.console.log("LOG_WS_RESULT:" + ws.id() + ":" + ws.name());
    }

    private void testUserBuilder() {
        User user = User.builder().id("u-1").name("User One").email("u1@ex.com").build();
        DomGlobal.console.log("LOG_USER_RESULT:" + user.id() + ":" + user.name());
    }

    private void testGroupBuilder() {
        Group group = Group.builder().id("g-1").workspace("ws-1").name("G1").build();
        DomGlobal.console.log("LOG_GROUP_RESULT:" + group.id() + ":" + group.workspace());
    }
}

package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Group;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Workspace;

import static elemental2.dom.DomGlobal.console;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        testBuilders();
        console.log("WORKSPACE_TEST_READY");
    }

    private void testBuilders() {
        Workspace ws = Workspace.create("ws-1", "My WS", "Desc");
        console.log("LOG_WS_RESULT:" + ws.id() + ":" + ws.name());

        User user = User.create("u-1", "U1", "u1@ex.com");
        console.log("LOG_USER_RESULT:" + user.id() + ":" + user.name());

        Group group = Group.create("g-1", "ws-1", "G1", "Desc");
        console.log("LOG_GROUP_RESULT:" + group.id() + ":" + group.workspace());
    }
}

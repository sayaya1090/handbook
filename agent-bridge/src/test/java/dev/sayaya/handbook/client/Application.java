package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.usecase.AgentMutation;
import dev.sayaya.handbook.usecase.AgentSearch;
import dev.sayaya.handbook.usecase.AgentState;
import dev.sayaya.handbook.usecase.WorkspaceEvent;
import dev.sayaya.rx.Observable;

import static elemental2.dom.DomGlobal.console;

public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        setupListeners();
        runTests();
        console.log("AGENT_BRIDGE_TEST_READY");
    }

    private void setupListeners() {
        AgentMutation.receiver().mutations().subscribe(changes -> {
            if (changes != null && changes.length > 0) {
                console.log("LOG_AGENT_MUTATION_RECEIVED:" + changes[0]);
            }
        });

        WorkspaceEvent.receiver().workspaceId().subscribe(id -> {
            if (id != null) console.log("LOG_WS_ID_RECEIVED:" + id);
        });
    }

    private void runTests() {
        // 1. Mutation 테스트
        AgentMutation.publish(new String[]{"test-change"});

        // 2. WorkspaceEvent 테스트
        WorkspaceEvent.publishId("ws-123");

        // 3. Search 테스트
        AgentSearch.register(query -> Observable.of("RESULT:" + query));
        AgentSearch.get().search("find-me").subscribe(res -> console.log("LOG_SEARCH_RESULT:" + res));

        // 4. State 테스트
        AgentState.register(() -> "MOCK_STATE");
        console.log("LOG_STATE_RESULT:" + AgentState.get().snapshot());
    }
}

package dev.sayaya.handbook.client.interfaces;

import com.google.gwt.core.client.GWT;
import dev.sayaya.handbook.client.domain.*;
import dev.sayaya.handbook.client.usecase.AgentApiPort;
import dev.sayaya.handbook.client.usecase.AgentSession;
import dev.sayaya.handbook.domain.AttentionStyle;
import elemental2.dom.EventSource;
import elemental2.dom.RequestInit;

import javax.inject.Inject;
import javax.inject.Singleton;

import static elemental2.dom.DomGlobal.fetch;

/**
 * Gateway를 통해 Assistant 서비스와 SSE로 통신한다.
 * 수신된 JSON 메시지를 파싱하여 CommandRouter에 전달한다.
 */
@Singleton
public class AgentSseClient implements AgentApiPort {
    private final CommandRouter router;
    private final AgentSession session;
    private EventSource eventSource;

    @Inject
    AgentSseClient(CommandRouter router, AgentSession session) {
        this.router = router;
        this.session = session;
    }

    @Override
    public void startSession(String workspace, String request) {
        session.stateObserver().next(AgentSessionState.PLANNING);

        RequestInit init = RequestInit.create();
        init.setMethod("POST");
        init.setBody("{\"request\":\"" + escapeJson(request) + "\"}");

        fetch("/workspace/" + workspace + "/assistant", init)
            .then(response -> {
                if (response.ok) {
                    connectSse(workspace);
                } else {
                    session.stateObserver().next(AgentSessionState.ABORTED);
                }
                return null;
            })
            .catch_(error -> {
                GWT.log("Agent session start failed: " + error);
                session.stateObserver().next(AgentSessionState.ABORTED);
                return null;
            });
    }

    private void connectSse(String workspace) {
        if (eventSource != null) eventSource.close();

        session.stateObserver().next(AgentSessionState.EXECUTING);
        eventSource = new EventSource("/workspace/" + workspace + "/assistant/stream");
        eventSource.onmessage = event -> {
            String data = event.data.toString();
            router.route(data);
        };
        eventSource.onerror = event -> {
            GWT.log("SSE connection error");
            session.stateObserver().next(AgentSessionState.ABORTED);
            closeSse();
        };
    }

    @Override
    public void respond(String workspace, String response) {
        session.stateObserver().next(AgentSessionState.EXECUTING);

        RequestInit init = RequestInit.create();
        init.setMethod("POST");
        init.setBody("{\"response\":\"" + escapeJson(response) + "\"}");

        fetch("/workspace/" + workspace + "/assistant/respond", init)
            .catch_(error -> {
                GWT.log("Agent respond failed: " + error);
                session.stateObserver().next(AgentSessionState.ABORTED);
                return null;
            });
    }

    @Override
    public void abort(String workspace) {
        RequestInit init = RequestInit.create();
        init.setMethod("POST");
        fetch("/workspace/" + workspace + "/assistant/abort", init);

        session.stateObserver().next(AgentSessionState.ABORTED);
        closeSse();
    }

    private void closeSse() {
        if (eventSource != null) {
            eventSource.close();
            eventSource = null;
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.usecase.WorkspaceEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;

/**
 * SessionContext의 상태 변경을 감지하여 사이드 이펙트(Event Bus 발행)를 담당하는 클래스.
 * SessionContext(상태)와 WorkspaceEvent(사이드 이펙트)의 책임을 분리합니다.
 */
@Singleton
public class WorkspaceEventPublisher {
    private final SessionContext sessionContext;
    private String currentWorkspaceId = null;

    @Inject
    WorkspaceEventPublisher(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    public void initialize() {
        sessionContext.subscribe(this::onContextChanged);
    }

    private void onContextChanged(Map<String, String> values) {
        String newWorkspaceId = values.get("workspaceId");
        if (!Objects.equals(currentWorkspaceId, newWorkspaceId)) {
            currentWorkspaceId = newWorkspaceId;
            WorkspaceEvent.publishId(newWorkspaceId);
        }
    }
}

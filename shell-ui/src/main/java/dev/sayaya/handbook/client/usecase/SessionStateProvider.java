package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.SessionState;
import dev.sayaya.handbook.domain.User;
import dev.sayaya.handbook.domain.Workspace;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

/**
 * {@link UserProvider} + {@link WorkspaceList} 를 조합해 현재 세션의 거시 상태를 방출한다.
 *
 * <p>규칙:</p>
 * <ul>
 *   <li>user == null → {@link SessionState#ANONYMOUS}</li>
 *   <li>user != null && workspaces.empty → {@link SessionState#AUTHENTICATED}</li>
 *   <li>user != null && workspaces.any → {@link SessionState#IN_WORKSPACE}</li>
 * </ul>
 *
 * <p>계층 추론 없음 규약과 짝 — 공급자 선언의 {@code allowedSessionStates} 집합과
 * 이 값의 {@link SessionState#kind()} 를 소비자(MenuRailItemElement 등) 가 멤버십 비교한다.</p>
 *
 * <p>Phase 2 확장: 활성 워크스페이스 선택 + role 세분화 도입 시 {@code IN_WORKSPACE_AS_ADMIN}
 * 분기를 추가한다. 이번 Phase 1 은 멤버 여부만 판정.</p>
 */
@Singleton
public class SessionStateProvider {
    @Delegate private final BehaviorSubject<SessionState> _this = behavior(SessionState.ANONYMOUS);
    private User user;
    private List<Workspace> workspaces = List.of();

    @Inject SessionStateProvider(UserProvider userProvider, WorkspaceList workspaces) {
        userProvider.subscribe(u -> { this.user = u; recompute(); });
        workspaces.distinctUntilChanged().subscribe(ws -> { this.workspaces = ws == null ? List.of() : ws; recompute(); });
    }

    private void recompute() {
        SessionState next;
        if (user == null) next = SessionState.ANONYMOUS;
        else if (workspaces.isEmpty()) next = SessionState.AUTHENTICATED;
        else next = SessionState.IN_WORKSPACE;
        SessionState current = getValue();
        if (current == null || current.kind() != next.kind()) next(next);
    }
}

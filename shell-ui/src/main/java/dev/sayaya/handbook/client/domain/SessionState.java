package dev.sayaya.handbook.client.domain;

import dev.sayaya.handbook.domain.SessionStateKind;

/**
 * 현재 세션의 거시 상태.
 *
 * <p>계약·요구사항: {@code docs/contracts/menus.md} §allowedSessionStates +
 * {@code docs/requirements.md} §3.24.</p>
 *
 * <p>{@link SessionStateKind} 하나로 표현할 수 있는 축이지만, 향후 Phase 2 에서
 * role 세분화({@code IN_WORKSPACE_AS_ADMIN}) 가 추가되면 이 클래스에 부가 정보를
 * 담을 수 있도록 abstract 로 둔다. Phase 1 은 정적 상수 3개로 충분.</p>
 *
 * <p>GWT 는 sealed class 미지원 — abstract + 정적 상수 패턴 사용.</p>
 */
public abstract class SessionState {
    public abstract SessionStateKind kind();

    public static final SessionState ANONYMOUS = new SessionState() {
        @Override public SessionStateKind kind() { return SessionStateKind.ANONYMOUS; }
    };
    public static final SessionState AUTHENTICATED = new SessionState() {
        @Override public SessionStateKind kind() { return SessionStateKind.AUTHENTICATED; }
    };
    public static final SessionState IN_WORKSPACE = new SessionState() {
        @Override public SessionStateKind kind() { return SessionStateKind.IN_WORKSPACE; }
    };
}

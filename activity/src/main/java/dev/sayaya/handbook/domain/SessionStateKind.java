package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * 사용자 세션의 거시 상태. {@code /menus} 응답의 {@code allowed_session_states}
 * 필드에서 공급자가 허용 상태 집합을 선언하고, shell-ui 소비자가 현재 세션 상태와
 * 집합 멤버십을 비교해 메뉴 가시성/활성/CTA 를 결정한다.
 *
 * <p><b>계층 추론 없음:</b> 평가는 단순 집합 멤버십이며, 상위 상태가 하위 상태를
 * 자동 포함하지 않는다. "로그인 이후 모두" 는 {@code {AUTHENTICATED, IN_WORKSPACE}}
 * 를 명시 열거해야 한다.</p>
 *
 * <p>계약: {@code docs/contracts/menus.md} §allowedSessionStates 규약.
 * 요구사항: {@code docs/requirements.md} §3.24.</p>
 *
 * <p><b>주의 (GWT):</b> {@code Menu#allowedSessionStates} wire 타입은
 * {@code String[]} ({@link #name()} 배열) 이다. 알 수 없는 값은 forward-compat
 * 로 무시(평가에서 매치 안 됨)된다.</p>
 *
 * <p><b>Phase 2 예약:</b> {@code IN_WORKSPACE_AS_ADMIN} 등 role 세분화 값 추가
 * 예정. 추가 시 평가 알고리즘은 그대로 집합 멤버십 — 공급자가 필요한 모든 값을 열거.</p>
 */
@JsType
public enum SessionStateKind {
    /** 인증 없음 (쿠키 무효/부재). 익명 사용자에게 노출되어야 하는 메뉴용. */
    @JsIgnore ANONYMOUS,
    /** 로그인 + 활성 워크스페이스 미선택. 워크스페이스 생성·참여 유도 화면에서 의미 있는 상태. */
    @JsIgnore AUTHENTICATED,
    /** 로그인 + 활성 워크스페이스 선택. 타입·문서·설정 등 도메인 진입 후 메뉴용. */
    @JsIgnore IN_WORKSPACE
    // TODO (Phase 2): IN_WORKSPACE_AS_ADMIN — role 세분화 후 추가.
}

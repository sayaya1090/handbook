package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * 네비게이션 메뉴 항목 도메인 객체.
 *
 * <p><b>책임:</b> Gateway에서 수신한 메뉴 JSON을 JsInterop으로 매핑하고, 제목/아이콘/스크립트/도구 배열/URL 패턴 등을 보유한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link Tool} — 메뉴 하위 도구 항목</li>
 * </ul></p>
 * <p><b>주의:</b> 네이티브 JsType이므로 equals/hashCode/toBuilder는 @JsOverlay로 구현된다.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class Menu {
    private String title;
    // snake_case 필드들은 JsInterop(GWT 프론트) 용 @JsProperty 와 Jackson(Spring 서버)
    // 용 @JsonProperty 를 **이원 병기** 한다. @JsProperty 는 Jackson 이 무시하고,
    // @JsonProperty 는 JsInterop 이 무시하므로 서로 간섭 없이 양쪽에서 같은 wire 이름을
    // 강제. 각 서비스의 ObjectMapper 가 SNAKE_CASE 전략을 설정했는지와 무관하게 일관된
    // 이름이 보장된다 (regression 2026-04: login 의 기본 ObjectMapper camelCase 직렬화
    // 때문에 Gateway 역직렬화 실패로 app_bar_slot/icon_type 필드가 null 이 되던 이슈).
    @JsProperty(name = "supporting_text")
    @JsonProperty("supporting_text")
    private String supportingText;
    @JsProperty(name = "icon_type")
    @JsonProperty("icon_type")
    private String iconType;
    private String icon;
    @JsProperty(name = "trailing_text")
    @JsonProperty("trailing_text")
    private String trailingText;
    private String script;   // 임포트할 스크립트
    private String order;
    private Tool[] tools;
    private Boolean bottom;
    /**
     * AppBar slot — shell-ui 의 {@code ShellAppBarElement} 에 승격할 메뉴의 배치 위치.
     * {@code "leading" | "center" | "trailing" | null}. null 이면 기존 Rail/Tabs 네비게이션
     * 축으로 렌더. 비-null 이면 네비게이션에서 제외되고 AppBar 해당 slot 으로 이동.
     *
     * <p>용도: 세션 액션(Sign In/Out) 처럼 네비게이션이 아닌 전역 액션 성격 메뉴를 MD3
     * Top App Bar trailing 관용 위치로 이동시킨다. 계약은 {@code docs/contracts/menus.md}
     * 의 "AppBar slot 승격" 섹션 참조.</p>
     */
    @JsProperty(name = "app_bar_slot")
    @JsonProperty("app_bar_slot")
    private String appBarSlot;
    @JsProperty(name = "url")
    @JsonProperty("url")
    private String url;
    @JsProperty(name = "url_regex")
    @JsonProperty("url_regex")
    private String[] urlRegex;
    /**
     * 이 메뉴가 노출되어야 하는 세션 상태 집합. 각 원소는 {@link SessionStateKind} 의
     * {@code name()} 값 (예: {@code "ANONYMOUS"}, {@code "AUTHENTICATED"}, {@code "IN_WORKSPACE"}).
     *
     * <p><b>의미론:</b></p>
     * <ul>
     *   <li>{@code null} — 무제약 (모든 세션 상태에서 상시 노출). default.</li>
     *   <li>빈 배열 {@code []} — "어떤 상태에서도 숨김" (공급자가 의도적으로 메뉴를 꺼둘 때).</li>
     *   <li>원소가 있는 배열 — 소비자는 현재 {@code SessionState.kind} 가 이 집합에 포함되는지 비교한다.</li>
     * </ul>
     *
     * <p><b>주의 — 계층 추론 없음 (명시 열거 필수):</b> 평가는 단순 집합 멤버십이다.
     * 상위 상태가 하위 상태를 자동 포함하지 않는다. 예를 들어 "로그인 이후 모든 사용자에게
     * 보여야 하는" 메뉴는 {@code {AUTHENTICATED, IN_WORKSPACE}} 두 값을 모두 열거해야
     * 한다 — {@code {AUTHENTICATED}} 만 선언하면 {@code IN_WORKSPACE} 사용자에게는
     * 보이지 않는다.</p>
     *
     * <p>계약 전체는 {@code docs/contracts/menus.md} §"allowedSessionStates 규약" 과
     * {@code docs/requirements.md §3.24} 참조. wire 이름은 {@code allowed_session_states}
     * (snake_case) 이며, 미디어타입은 v1 유지 (additive 필드).</p>
     *
     * <p>필드 타입을 {@code String[]} 으로 유지하는 이유: {@code @JsType(isNative=true)}
     * 경계를 넘는 JSON 역직렬화가 JS primitive 배열만 허용하기 때문. enum 로의 변환은
     * {@link #allowedSessionStatesSet()} / {@link #isAllowedFor(SessionStateKind)}
     * 헬퍼가 담당한다.</p>
     */
    @JsProperty(name = "allowed_session_states")
    @JsonProperty("allowed_session_states")
    private String[] allowedSessionStates;

    @Override @JsOverlay @JsIgnore
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return Objects.equals(title, menu.title);
    }
    @Override @JsOverlay @JsIgnore
    public int hashCode() {
        return Objects.hash(title);
    }

    @JsOverlay @JsIgnore
    public static MenuBuilder builder() {
        return new MenuBuilder();
    }
    @JsOverlay @JsIgnore
    public MenuBuilder toBuilder() {
        return new MenuBuilder().title(this.title).supportingText(this.supportingText).iconType(this.iconType).icon(this.icon)
                .trailingText(this.trailingText).script(this.script).order(this.order).tools(this.tools).bottom(this.bottom)
                .appBarSlot(this.appBarSlot).url(this.url).urls(this.urlRegex).allowedSessionStates(this.allowedSessionStates);
    }

    /**
     * {@link #allowedSessionStates} 를 {@link SessionStateKind} 집합으로 변환해 반환.
     *
     * <p>반환 규약:</p>
     * <ul>
     *   <li>wire 필드가 {@code null} → {@code null} 반환 (무제약 의미 보존 — 빈 집합과 구분)</li>
     *   <li>wire 필드가 빈 배열 {@code []} → 빈 {@link Set} 반환 ("어떤 상태에서도 숨김")</li>
     *   <li>원소가 있으면 UPPER_SNAKE_CASE 문자열을 enum 으로 파싱. 알 수 없는 값은 무시</li>
     * </ul>
     *
     * <p>소비자(shell-ui)가 가시성 평가 알고리즘에서 이 헬퍼를 사용한다. 서버 공급자 코드는
     * {@link MenuBuilder#allowedSessionStates(SessionStateKind...)} 로 주입하면 됨.</p>
     */
    @JsOverlay @JsIgnore
    public Set<SessionStateKind> allowedSessionStatesSet() {
        if (allowedSessionStates == null) return null;
        Set<SessionStateKind> out = new HashSet<>();
        for (String raw : allowedSessionStates) {
            if (raw == null) continue;
            try {
                out.add(SessionStateKind.valueOf(raw));
            } catch (IllegalArgumentException ignored) {
                // 구/신 버전 skew 시 미지의 값은 무시 — forward compat
            }
        }
        return out;
    }

    /**
     * 주어진 세션 상태 {@code kind} 에서 이 메뉴가 노출되어야 하는지 판단.
     *
     * <p>{@code allowedSessionStates} 가 {@code null} 이면 항상 {@code true} (무제약).
     * 빈 배열이면 항상 {@code false}. 값이 있으면 집합 멤버십 검사.</p>
     *
     * <p><b>주의:</b> 계층 추론 없음 — {@code {AUTHENTICATED}} 만 선언된 메뉴는
     * {@link SessionStateKind#IN_WORKSPACE} 에서 {@code false} 를 돌려준다.</p>
     */
    @JsOverlay @JsIgnore
    public boolean isAllowedFor(SessionStateKind kind) {
        if (allowedSessionStates == null) return true;
        if (kind == null) return false;
        String target = kind.name();
        for (String raw : allowedSessionStates) {
            if (target.equals(raw)) return true;
        }
        return false;
    }
    @Setter
    @Accessors(fluent = true)
    public static class MenuBuilder {
        private String title;
        private String supportingText;
        private String iconType;
        private String icon;
        private String trailingText;
        private String script;
        private String order;
        private List<Tool> tools = new LinkedList<>();
        private Boolean bottom;
        private String appBarSlot;
        private String url;
        private List<String> urlRegex = new LinkedList<>();
        private String[] allowedSessionStates; // null = 무제약, [] = 어떤 상태에서도 숨김
        private MenuBuilder() {}
        /**
         * 세션 상태 집합을 enum varargs 로 주입. 공급자 코드에서 가장 권장되는 진입점.
         *
         * <p>예: {@code .allowedSessionStates(SessionStateKind.AUTHENTICATED, SessionStateKind.IN_WORKSPACE)}</p>
         *
         * <p>계층 추론이 없으므로 "로그인 이후 모두에게 보여야 하는" 메뉴는 두 값 모두 열거할 것.
         * null varargs → 무제약 default 와 동일. 빈 varargs → 빈 배열 저장 (모두 숨김).</p>
         */
        public MenuBuilder allowedSessionStates(SessionStateKind... kinds) {
            if (kinds == null) {
                this.allowedSessionStates = null;
                return this;
            }
            String[] out = new String[kinds.length];
            for (int i = 0; i < kinds.length; i++) {
                out[i] = kinds[i].name();
            }
            this.allowedSessionStates = out;
            return this;
        }
        /**
         * 세션 상태 집합을 {@link Set} 로 주입. enum 컬렉션을 보유 중인 경우 편의용.
         * 내부적으로 {@code SessionStateKind.name()} 배열로 직렬화된다.
         */
        public MenuBuilder allowedSessionStates(Set<SessionStateKind> kinds) {
            if (kinds == null) {
                this.allowedSessionStates = null;
                return this;
            }
            this.allowedSessionStates = kinds.stream().map(SessionStateKind::name).toArray(String[]::new);
            return this;
        }
        // wire 레벨 setter — toBuilder() 의 원본 String[] 복제용. 공급자 코드는
        // 위 enum varargs / Set 오버로드 사용 권장.
        // (Lombok @Setter 가 자동 생성할 예정이었으나 enum 오버로드와 시그니처 충돌로
        //  실제로는 생성되지 않아 명시적으로 선언)
        public MenuBuilder allowedSessionStates(String[] raw) {
            this.allowedSessionStates = raw;
            return this;
        }
        public MenuBuilder tool(Tool tool) {
            this.tools.add(tool);
            return this;
        }
        public MenuBuilder tools(Collection<Tool> tools) {
            this.tools.addAll(tools);
            return this;
        }
        public MenuBuilder tools(Tool... tools) {
            return tools(Arrays.asList(tools));
        }
        public MenuBuilder url(String url) {
            this.url = url;
            return this;
        }
        public MenuBuilder urls(Collection<String> urls) {
            this.urlRegex.addAll(urls);
            return this;
        }
        public MenuBuilder urls(String... urls) {
            return urls(Arrays.asList(urls));
        }
        public Menu build() {
            var menu = new Menu();
            menu.title = this.title;
            menu.supportingText = this.supportingText;
            menu.iconType = this.iconType;
            menu.icon = this.icon;
            menu.trailingText = this.trailingText;
            menu.script = this.script;
            menu.order = this.order;
            menu.tools = this.tools.stream().toArray(Tool[]::new);
            menu.bottom = this.bottom;
            menu.appBarSlot = this.appBarSlot;
            menu.url = this.url;
            menu.urlRegex = this.urlRegex.stream().toArray(String[]::new);
            menu.allowedSessionStates = this.allowedSessionStates; // null 유지가 기본 (무제약 default)
            return menu;
        }
    }
}

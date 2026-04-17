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
    @JsProperty(name = "url_regex")
    @JsonProperty("url_regex")
    private String[] urlRegex;

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
                .appBarSlot(this.appBarSlot).urls(this.urlRegex);
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
        private List<String> urlRegex = new LinkedList<>();
        private MenuBuilder() {}
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
            this.urlRegex.add(url);
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
            menu.urlRegex = this.urlRegex.stream().toArray(String[]::new);
            return menu;
        }
    }
}

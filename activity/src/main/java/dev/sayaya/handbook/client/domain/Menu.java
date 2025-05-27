package dev.sayaya.handbook.client.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class Menu {
    private String title;
    @JsProperty(name = "supporting_text")
    private String supportingText;
    @JsProperty(name = "icon_type")
    private String iconType;
    private String icon;
    @JsProperty(name = "trailing_text")
    private String trailingText;
    private String script;   // 임포트할 스크립트
    private String order;
    private Tool[] tools;
    private Boolean bottom;
    @JsProperty(name = "url_regex")
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
                .urls(this.urlRegex);
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
            menu.urlRegex = this.urlRegex.stream().toArray(String[]::new);
            return menu;
        }
    }
}

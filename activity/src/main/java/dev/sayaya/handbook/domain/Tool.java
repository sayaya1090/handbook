package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * 도구 모음(ToolRail) 항목 도메인 객체.
 *
 * <p><b>책임:</b> 메뉴 하위의 개별 도구 정보(아이콘, 제목, 정렬 순서, 실행 함수)를 보유한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ToolFunction} — 도구 클릭 시 실행되는 콜백</li>
 * </ul></p>
 * <p><b>주의:</b> 네이티브 JsType이므로 빌더는 @JsOverlay로 구현된다.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class Tool {
    private String id;
    private String icon;
    // @JsProperty(GWT) + @JsonProperty(Spring) 이원 병기 — 각 서비스 ObjectMapper 가
    @JsProperty(name = "icon_type")
    @JsonProperty("icon_type")
    private String iconType;
    private String title;
    private String order;
    @JsProperty
    @JsonProperty("url")
    private String url;
    @JsProperty(name = "url_regex")
    @JsonProperty("url_regex")
    private String[] urlRegex;
    @Setter(onMethod_ = {@JsOverlay, @JsIgnore})
    private ToolFunction function;

    @Override @JsOverlay @JsIgnore
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tool tool = (Tool) o;
        if (id != null && tool.id != null) return Objects.equals(id, tool.id);
        return Objects.equals(title, tool.title);
    }
    @Override @JsOverlay @JsIgnore
    public int hashCode() {
        if (id != null) return Objects.hash(id);
        return Objects.hash(title);
    }

    @JsOverlay @JsIgnore
    public static ToolBuilder builder() {
        return new ToolBuilder();
    }
    @JsOverlay @JsIgnore
    public ToolBuilder toBuilder() {
        return new ToolBuilder().id(this.id).icon(this.icon).iconType(this.iconType).title(this.title).order(this.order).url(this.url).urlRegex(this.urlRegex).function(this.function);
    }
    @Setter
    @Accessors(fluent = true)
    public static class ToolBuilder {
        private String id;
        private String icon;
        private String iconType;
        private String title;
        private String order;
        private String url;
        private String[] urlRegex;
        private ToolFunction function;
        private ToolBuilder(){}
        public ToolBuilder id(String id) { this.id = id; return this; }
        public ToolBuilder icon(String icon) { this.icon = icon; return this; }
        public ToolBuilder iconType(String iconType) { this.iconType = iconType; return this; }
        public ToolBuilder title(String title) { this.title = title; return this; }
        public ToolBuilder order(String order) { this.order = order; return this; }
        public ToolBuilder url(String url) { this.url = url; return this; }
        public ToolBuilder urls(String... urls) {
            this.urlRegex = urls;
            return this;
        }
        public ToolBuilder function(ToolFunction function) { this.function = function; return this; }
        public Tool build() {
            var tool = new Tool();
            tool.id = id;
            tool.icon = icon;
            tool.iconType = iconType;
            tool.title = title;
            tool.order = order;
            tool.url = url;
            tool.urlRegex = urlRegex;
            tool.function = function;
            return tool;
        }
    }
}
package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
    private String icon;
    // @JsProperty(GWT) + @JsonProperty(Spring) 이원 병기 — 각 서비스 ObjectMapper 가
    // SNAKE_CASE 전략을 설정하지 않아도 wire 이름이 일관되게 snake_case 로 유지됨.
    @JsProperty(name = "icon_type")
    @JsonProperty("icon_type")
    private String iconType;
    private String title;
    private String order;
    @Setter(onMethod_ = {@JsOverlay, @JsIgnore})
    private ToolFunction function;
    @JsOverlay @JsIgnore
    public static ToolBuilder builder() {
        return new ToolBuilder();
    }
    @JsOverlay @JsIgnore
    public ToolBuilder toBuilder() {
        return new ToolBuilder().icon(this.icon).iconType(this.iconType).title(this.title).order(this.order).function(this.function);
    }
    @Setter
    @Accessors(fluent = true)
    public static class ToolBuilder {
        private String icon;
        private String iconType;
        private String title;
        private String order;
        private ToolFunction function;
        private ToolBuilder(){}
        public Tool build() {
            var tool = new Tool();
            tool.icon = icon;
            tool.iconType = iconType;
            tool.title = title;
            tool.order = order;
            tool.function = function;
            return tool;
        }
    }
}
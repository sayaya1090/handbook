package dev.sayaya.handbook.client.domain;

import dev.sayaya.handbook.client.usecase.ToolFunction;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class Tool {
    private String icon;
    @JsProperty(name = "icon_type")
    private String iconType;
    private String title;
    @JsProperty(name = "uri_regex")
    private String uriRegex;    // 해당 페이지로 간주할 URL 정규표현식
    private String order;
    @Setter(onMethod_ = {@JsOverlay, @JsIgnore})
    private ToolFunction function;
    @JsOverlay @JsIgnore
    public static ToolBuilder builder() {
        return new ToolBuilder();
    }
    @JsOverlay @JsIgnore
    public ToolBuilder toBuilder() {
        return new ToolBuilder().icon(this.icon).iconType(this.iconType).title(this.title).uriRegex(this.uriRegex).order(this.order).function(this.function);
    }
    @Setter
    @Accessors(fluent = true)
    public static class ToolBuilder {
        private String icon;
        private String iconType;
        private String title;
        private String uriRegex;
        private String order;
        private ToolFunction function;
        private ToolBuilder(){}
        public Tool build() {
            var tool = new Tool();
            tool.icon = icon;
            tool.iconType = iconType;
            tool.title = title;
            tool.uriRegex = uriRegex;
            tool.order = order;
            tool.function = function;
            return tool;
        }
    }
}
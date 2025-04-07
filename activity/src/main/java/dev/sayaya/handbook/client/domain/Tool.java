package dev.sayaya.handbook.client.domain;

import dev.sayaya.handbook.client.usecase.ToolFunction;
import jsinterop.annotations.*;
import lombok.Setter;
import lombok.experimental.Accessors;

@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="Object")
@Setter(onMethod_ = { @JsOverlay, @JsIgnore } )
@Accessors(fluent = true)
public final class Tool {
    public String icon;
    @JsProperty(name="icon_type")
    public String iconType;
    public String title;
    public String script;   // 임포트할 스크립트
    public String uri;      // 쉘에 전달할 초기 URL(페이지 식별자)
    @JsProperty(name="uri_regex")
    public String uriRegex;    // 해당 페이지로 간주할 URL 정규표현식
    public String order;
    public ToolFunction onClick;
}
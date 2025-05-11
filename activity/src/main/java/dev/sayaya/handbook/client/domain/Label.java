package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import lombok.Getter;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace= JsPackage.GLOBAL, name="Object")
@Accessors(fluent = true)
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
public final class Label {
    private String fontUrl;
    private String mdRefTypefacePlain;
    private String mdSysTypescaleHeadline;
    private String mdSysTypescaleLabel;
    private String mdSysTypescaleBody;

    @JsOverlay
    @JsIgnore
    public static String findLabelOrDefault(Label label, String key) {
        if(label == null) return key;
        return Js.asPropertyMap(label).has(key) ? Js.asPropertyMap(label).get(key).toString() : key;
    }
}

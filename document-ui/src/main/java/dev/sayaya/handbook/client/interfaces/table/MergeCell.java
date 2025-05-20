package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.experimental.Accessors;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
@Accessors(fluent=true)
public final class MergeCell {
    private double row;
    private double col;
    private double rowspan;
    private double colspan;
}

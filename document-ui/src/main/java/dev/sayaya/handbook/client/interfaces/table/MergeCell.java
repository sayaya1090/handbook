package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.experimental.Accessors;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
@Accessors(fluent=true)
public final class MergeCell {
    public double row;
    public double col;
    public double rowspan;
    public double colspan;
}

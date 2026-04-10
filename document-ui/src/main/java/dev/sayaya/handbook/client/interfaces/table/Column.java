package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** Handsontable 컬럼 설정. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class Column {
    public String data;
    public String type;
    public Integer width;
    public Boolean readOnly;
}

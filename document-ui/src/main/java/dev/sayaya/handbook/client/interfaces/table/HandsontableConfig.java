package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/** Handsontable 설정 객체. native JS Object로 전달된다. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class HandsontableConfig {
    public Object[][] data;
    public String stretchH;
    public Object width;
    public Object height;
    public Integer minRows;
    public boolean manualColumnResize;
    public boolean autoRowSize;
    public boolean autoColSize;
    public Column[] columns;
    public Object colHeaders;
    public boolean readOnly;
    public Integer fixedColumnsLeft;
    public String licenseKey;
}

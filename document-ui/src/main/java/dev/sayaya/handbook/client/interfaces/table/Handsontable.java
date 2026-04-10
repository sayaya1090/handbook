package dev.sayaya.handbook.client.interfaces.table;

import elemental2.dom.Element;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** Handsontable JS 라이브러리의 JsInterop 래퍼. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Handsontable")
public final class Handsontable {
    @JsConstructor
    public Handsontable(Element element, HandsontableConfig setting) {}

    @JsProperty
    public Element container;

    public native void render();
    public native void updateSettings(HandsontableConfig setting);
    public native HandsontableConfig getSettings();
    public native int countRows();
    public native int countCols();
    public native boolean selectCell(int row, int column);
    public native void deselectCell();
    public native Element getCell(int row, int col, boolean topmost);
    public native void setDataAtCell(int row, int col, Object value);
    public native void alter(String action, int index, int amount);
    public native void destroy();
}

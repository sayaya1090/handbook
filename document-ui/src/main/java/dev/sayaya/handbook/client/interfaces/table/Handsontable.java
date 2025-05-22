package dev.sayaya.handbook.client.interfaces.table;

import elemental2.dom.Element;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Handsontable")
public final class Handsontable {
    @JsConstructor public Handsontable(Element element, HandsontableConfiguration setting) {}
    public native void render();
    public native void updateSettings(HandsontableConfiguration setting);
    public native HandsontableConfiguration getSettings();
    public native int countRows();
    public native int countCols();
    public native boolean selectColumns(int start, int end);
    public native boolean selectColumns(int start);
    public native boolean selectRows(int start, int end);
    public native boolean selectRows(int start);
    public native boolean selectCell(int row, int column);
    public native void deselectCell();
    public native Element getCell(int row, int col, boolean topmost);
    public native void setDataAtCell(int row, int col, Object value);
    public native void alter(String action, int idex, int amount);
}


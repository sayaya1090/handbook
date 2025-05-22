package dev.sayaya.handbook.client.interfaces.table.function;

import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;

@JsFunction
public interface CellRenderer {
    HTMLElement render(Handsontable instance, HTMLTableCellElement td, int row, int col, String prop, String value, Column columnInfo);
    @JsOverlay
    default String getFont() {
        return "'Montserrat', 'Noto Sans KR', sans-serif";
    }
    @JsOverlay
    default int getFontSize() {
        return 12;
    }
}
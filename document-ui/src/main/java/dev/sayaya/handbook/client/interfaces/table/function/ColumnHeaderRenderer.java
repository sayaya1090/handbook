package dev.sayaya.handbook.client.interfaces.table.function;

import elemental2.dom.HTMLTableCellElement;
import jsinterop.annotations.JsFunction;

@JsFunction
public interface ColumnHeaderRenderer {
    HTMLTableCellElement accept(int col, HTMLTableCellElement th);
}

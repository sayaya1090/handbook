package dev.sayaya.handbook.client.interfaces.table.function;

import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.MouseEvent;
import jsinterop.annotations.JsFunction;

@JsFunction
public interface MouseEventHandler {
    void apply(MouseEvent event, CellCoordinate coords, HTMLTableCellElement td);
}

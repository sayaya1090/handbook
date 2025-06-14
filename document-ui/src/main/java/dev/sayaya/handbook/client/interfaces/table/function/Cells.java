package dev.sayaya.handbook.client.interfaces.table.function;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface Cells {
    Object properties(int row, int col, String prop);
}

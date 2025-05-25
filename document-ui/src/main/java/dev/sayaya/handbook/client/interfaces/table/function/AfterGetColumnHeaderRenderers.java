package dev.sayaya.handbook.client.interfaces.table.function;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface AfterGetColumnHeaderRenderers {
    void apply(ColumnHeaderRenderer[] renderers);
}

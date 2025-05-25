package dev.sayaya.handbook.client.interfaces.table.column;

import elemental2.dom.HTMLElement;

@FunctionalInterface
public interface ColumnStyleFn<T> {
    T apply(HTMLElement td, int row, String prop, String value);
}

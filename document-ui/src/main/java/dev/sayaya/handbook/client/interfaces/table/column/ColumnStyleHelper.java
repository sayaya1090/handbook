package dev.sayaya.handbook.client.interfaces.table.column;

import elemental2.dom.HTMLElement;

public interface ColumnStyleHelper<SELF> {
    SELF clear(HTMLElement td);
    HTMLElement apply(HTMLElement td, int row, String prop, String value);
}

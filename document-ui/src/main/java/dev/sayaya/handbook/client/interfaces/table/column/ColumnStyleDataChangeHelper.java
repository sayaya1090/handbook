package dev.sayaya.handbook.client.interfaces.table.column;

import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;

import java.util.function.Supplier;

public final class ColumnStyleDataChangeHelper<SELF> {
    private final Supplier<SELF> _self;
    public ColumnStyleDataChangeHelper(Supplier<SELF> columnBuilder) {
        _self = columnBuilder;
    }
    public HTMLElement apply(Handsontable instance, HTMLTableCellElement td, int row, String prop) {
        Data data = instance.getSettings().data[row];
        if(data!=null && data.isChanged(prop)) td.classList.add("changed");
        else td.classList.remove("changed");
        return td;
    }
    private SELF that() {
        return _self.get();
    }
}

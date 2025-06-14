package dev.sayaya.handbook.client.interfaces.table.column;

import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;

import java.util.function.Supplier;

final class ColumnStyleDataDeleteHelper<SELF> {
    private final Supplier<SELF> _self;
    public ColumnStyleDataDeleteHelper(Supplier<SELF> columnBuilder) {
        _self = columnBuilder;
    }
    public HTMLElement apply(Handsontable instance, HTMLTableCellElement td, int row, String prop) {
        Data data = instance.getSettings().data[row];
        if(data==null || !deleted(data)) td.classList.remove("deleted");
        else td.classList.add("deleted");
        return td;
    }
    public boolean deleted(Data data) {
        var state = data.get("$state");
        return "DELETE".equals(state);
    }
    private SELF that() {
        return _self.get();
    }
}

package dev.sayaya.handbook.client.interfaces.table.column;

import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;

import java.util.function.Supplier;

public final class ColumnStyleDataValidateHelper<SELF> {
    private final Supplier<SELF> _self;
    public ColumnStyleDataValidateHelper(Supplier<SELF> columnBuilder) {
        _self = columnBuilder;
    }
    public HTMLElement apply(Handsontable instance, HTMLTableCellElement td, int row, String prop) {
        Data data = instance.getSettings().data[row];
        if(data==null) td.classList.remove("valid", "invalid");
        else {
            var valid = data.isValid(prop);
            if(valid==null) td.classList.remove("valid", "invalid");
            else if(valid) td.classList.add("valid");
            else td.classList.add("invalid");
        }
        return td;
    }
    private SELF that() {
        return _self.get();
    }
}

package dev.sayaya.handbook.client.interfaces.table.column;

import elemental2.dom.HTMLElement;

import java.util.function.Supplier;

public final class ColumnStyleAlignHelper<SELF> implements ColumnStyleHelper<SELF> {
    private final Supplier<SELF> _self;
    private String horizontal;
    private String vertical;
    public ColumnStyleAlignHelper(Supplier<SELF> columnBuilder) {
        _self = columnBuilder;
    }
    @Override
    public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
        if(horizontal !=null)	td.style.textAlign  = horizontal;
        if(vertical!=null)  	td.style.verticalAlign  = vertical;
        return td;
    }
    @Override
    public SELF clear(HTMLElement td) {
        td.style.removeProperty("text-align");
        td.style.removeProperty("vertical-align");
        return that();
    }
    public SELF horizontal(String horizontal) {
        this.horizontal = horizontal;
        return that();
    }
    public SELF vertical(String vertical) {
        this.vertical = vertical;
        return that();
    }
    private SELF that() {
        return _self.get();
    }
}

package dev.sayaya.handbook.client.interfaces.table.column;

import elemental2.dom.HTMLElement;

import java.util.function.Supplier;

public final class ColumnStyleColorHelper<SELF> implements ColumnStyleHelper<SELF> {
    private final Supplier<SELF> _self;
    private ColumnStyleFn<String> color;
    private ColumnStyleFn<String> colorBackground;
    public ColumnStyleColorHelper(Supplier<SELF> columnBuilder) {
        _self = columnBuilder;
    }
    @Override
    public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
        if(color!=null)             td.style.color              = color.apply(td, row, prop, value);
        if(colorBackground!=null)   td.style.backgroundColor    = colorBackground.apply(td, row, prop, value);
        return td;
    }
    @Override
    public SELF clear(HTMLElement td) {
        td.style.removeProperty("color");
        td.style.removeProperty("background-color");
        return that();
    }
    public SELF color(String color) {
        if(color == null) return color((ColumnStyleFn<String>)null);
        return color((td, row, prop, value)->color);
    }
    public SELF color(ColumnStyleFn<String> color) {
        this.color = color;
        return that();
    }
    public SELF colorBackground(String colorBackground) {
        if(colorBackground == null) return colorBackground((ColumnStyleFn<String>)null);
        return colorBackground((td, row, prop, value)->colorBackground);
    }
    public SELF colorBackground(ColumnStyleFn<String> colorBackground) {
        this.colorBackground = colorBackground;
        return that();
    }
    private SELF that() {
        return _self.get();
    }
}

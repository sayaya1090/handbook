package dev.sayaya.handbook.client.interfaces.table.column;

import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLElement;

import java.util.function.Supplier;

public final class ColumnStyleTextHelper<SELF> implements ColumnStyleHelper<SELF> {
    private final Supplier<SELF> _self;
    private ColumnStyleFn<String> font;
    private ColumnStyleFn<CSSProperties.FontSizeUnionType> fontSize;
    private ColumnStyleFn<Boolean> bold;
    private ColumnStyleFn<Boolean> italic;
    public ColumnStyleTextHelper(Supplier<SELF> columnBuilder) {
        _self = columnBuilder;
    }
    @Override
    public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
        if(font!=null)      td.style.fontFamily = font.apply(td, row, prop, value);
        if(fontSize!=null)  td.style.fontSize   = fontSize.apply(td, row, prop, value);
        if(bold!=null)      td.style.fontWeight = bold.apply(td, row, prop, value)?"bold":"normal";
        if(italic!=null)    td.style.fontStyle  = italic.apply(td, row, prop, value)?"italic":"normal";
        return td;
    }
    @Override
    public SELF clear(HTMLElement td) {
        td.style.removeProperty("font-family");
        td.style.removeProperty("font-size");
        td.style.removeProperty("font-weight");
        td.style.removeProperty("font-style");
        return that();
    }
    public SELF font(String font) {
        if(font == null) return font((ColumnStyleFn<String>)null);
        return font((td, row, prop, value)->font);
    }
    public SELF font(ColumnStyleFn<String> font) {
        this.font = font;
        return that();
    }
    public SELF fontSize(CSSProperties.FontSizeUnionType size) {
        if(size == null) return fontSize((ColumnStyleFn<CSSProperties.FontSizeUnionType>)null);
        return fontSize((td, row, prop, value)->size);
    }
    public SELF fontSize(ColumnStyleFn<CSSProperties.FontSizeUnionType> fontSize) {
        this.fontSize = fontSize;
        return that();
    }
    public SELF bold(boolean bold) {
        return bold((td, row, prop, value)->bold);
    }
    public SELF bold(ColumnStyleFn<Boolean> bold) {
        this.bold = bold;
        return that();
    }
    public SELF italic(boolean italic) {
        return italic((td, row, prop, value)->italic);
    }
    public SELF italic(ColumnStyleFn<Boolean> italic) {
        this.italic = italic;
        return that();
    }
    private SELF that() {
        return _self.get();
    }
}

package dev.sayaya.handbook.client.interfaces.table.column;

import elemental2.dom.HTMLElement;

import java.util.function.Supplier;

public final class ColumnStyleColorRangeHelper<SELF> implements ColumnStyleHelper<SELF> {
    enum Operation {
        EQ, LT, GT, LE, GE, BW, BWE
    }
    private final Operation op;
    private final Double param1;
    private final Double param2;
    private ColumnStyleFn<String> color;
    private ColumnStyleFn<String> colorBackground;
    private final Supplier<SELF> _self;
    ColumnStyleColorRangeHelper(Operation op, double param1, Supplier<SELF> columnBuilder) {
        this(op, param1, null, columnBuilder);
    }
    ColumnStyleColorRangeHelper(Operation op, double param1, Double param2, Supplier<SELF> columnBuilder) {
        this.op = op;
        this.param1 = param1;
        this.param2 = param2;
        _self = columnBuilder;
    }
    @Override
    public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
        if(value == null || value.trim().isEmpty()) return td;
        Double parse = Double.parseDouble(value);
        boolean match = false;
        try {switch(op) {
            case EQ: match = (parse.equals(param1));                break;
            case LT: match = (parse < param1);                      break;
            case GT: match = (parse > param1);                      break;
            case LE: match = (parse <= param1);                     break;
            case GE: match = (parse >= param1);                     break;
            case BW: match = (parse > param1 && parse < param2);    break;
            case BWE: match = (parse >= param1 && parse <= param2); break;
            default:
        }} catch(Exception e) { return td; }
        if(match) {
            if(color !=null)             td.style.color              = color.apply(td, row, prop, value);
            if(colorBackground !=null)   td.style.backgroundColor    = colorBackground.apply(td, row, prop, value);
        }
        return td;
    }
    @Override
    public SELF clear(HTMLElement td) {
        td.style.removeProperty("color");
        td.style.removeProperty("background-color");
        return that();
    }
    public SELF than(String color) {
        if(color == null) return than((ColumnStyleFn<String>)null);
        return than((td, row, prop, value)->color);
    }
    public SELF than(ColumnStyleFn<String> color) {
        this.color = color;
        return that();
    }
    public SELF than(String color, String background) {
        if(background == null) return than(color);
        if(color == null) return than(null, (td, row, prop, value)->background);
        return than((td, row, prop, value)->color, (td, row, prop, value)->background);
    }
    public SELF than(ColumnStyleFn<String> color, ColumnStyleFn<String> background) {
        this.color = color;
        this.colorBackground = background;
        return that();
    }
    private SELF that() {
        return _self.get();
    }
}

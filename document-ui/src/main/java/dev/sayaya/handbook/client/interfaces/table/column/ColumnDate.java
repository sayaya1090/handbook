package dev.sayaya.handbook.client.interfaces.table.column;

import com.google.gwt.i18n.client.DateTimeFormat;
import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditor;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditorFactory;
import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;
import elemental2.dom.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.span;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Setter
@Accessors(fluent = true)
public final class ColumnDate implements ColumnBuilder {
    private final static JsRegExp CHK_NUMBER = new JsRegExp("^\\d+$");
    private final String id;
    private DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
    @Delegate(excludes = ColumnBuilder.class) private final ColumnBuilderDefaultHelper<ColumnDate> defaultHelper = new ColumnBuilderDefaultHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleTextHelper<ColumnDate> textHelper = new ColumnStyleTextHelper<>(()->this);
    private final ColumnStyleDataChangeHelper<ColumnDate> dataChangeHelper = new ColumnStyleDataChangeHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleColorHelper<ColumnDate> colorHelper = new ColumnStyleColorHelper<>(()->this);
    private final List<ColumnStyleColorRangeHelper<ColumnDate>> colorRangeHelpers = new LinkedList<>();
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleAlignHelper<ColumnDate> alignHelper = new ColumnStyleAlignHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleDataValidateHelper<ColumnDate> dataValidateHelper = new ColumnStyleDataValidateHelper<>(()->this);
    private static String toString(DateTimeFormat DTF, Object value) throws RuntimeException {
        if(value == null)                   return null;
        else if(value instanceof Long)      return DTF.format(new Date((Long)value));
        else if(value instanceof Integer)   return DTF.format(new Date((Integer)value));
        else if(value instanceof Double)    return DTF.format(new Date(((Double)value).longValue()));
        else if(value instanceof String cast) {
            try {
                cast = cast.trim();
                RegExpResult chkNumber = CHK_NUMBER.exec(cast);
                if (chkNumber != null) return DTF.format(new Date(Long.parseLong(cast)));
                else return DTF.format(DTF.parse(cast));
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
                return cast;
            }
        } else throw new RuntimeException();
    }
    @Override
    public Column build() {
        Column column = defaultHelper.build().data(id).header(id);
        return column.renderer((instance, td, row, col, prop, value, ci)->{
                    textHelper.clear(td);
                    colorHelper.clear(td);
                    for(var helper: colorRangeHelpers) helper.clear(td);
                    alignHelper.clear(td);

                    textHelper.apply(td, row, prop, value);
                    colorHelper.apply(td, row, prop, value);
                    dataChangeHelper.apply(instance, td, row, prop);
                    dataValidateHelper.apply(instance, td, row, prop);
                    Date parse = null;
                    try {parse = format.parse(value);}catch(Exception ignore) {}
                    for(var helper: colorRangeHelpers) helper.apply(td, row, prop, parse!=null?String.valueOf(parse.getTime()):null);
                    alignHelper.apply(td, row, prop, value);
                    td.innerHTML = toString(format, value);
                    return td;
                }).editor(this::dateFieldEditor)
                .headerRenderer(n->span().text(defaultHelper.name()).element());
    }
    public ColumnStyleColorRangeHelper<ColumnDate> eq(Date value) {
        return range(ColumnStyleColorRangeHelper.Operation.EQ, value);
    }
    public ColumnStyleColorRangeHelper<ColumnDate> lt(Date value) {
        return range(ColumnStyleColorRangeHelper.Operation.LT, value);
    }
    public ColumnStyleColorRangeHelper<ColumnDate> gt(Date value) {
        return range(ColumnStyleColorRangeHelper.Operation.GT, value);
    }
    public ColumnStyleColorRangeHelper<ColumnDate> le(Date value) {
        return range(ColumnStyleColorRangeHelper.Operation.LE, value);
    }
    public ColumnStyleColorRangeHelper<ColumnDate> ge(Date value) {
        return range(ColumnStyleColorRangeHelper.Operation.GE, value);
    }
    public ColumnStyleColorRangeHelper<ColumnDate> bw(Date less, Date grater) {
        return range(ColumnStyleColorRangeHelper.Operation.BW, less, grater);
    }
    public ColumnStyleColorRangeHelper<ColumnDate> bwe(Date less, Date grater) {
        return range(ColumnStyleColorRangeHelper.Operation.BWE, less, grater);
    }
    private ColumnStyleColorRangeHelper<ColumnDate> range(ColumnStyleColorRangeHelper.Operation op, Date value) {
        ColumnStyleColorRangeHelper<ColumnDate> helper = new ColumnStyleColorRangeHelper<>(op, value.getTime(), ()->this);
        colorRangeHelpers.add(helper);
        return helper;
    }
    private ColumnStyleColorRangeHelper<ColumnDate> range(ColumnStyleColorRangeHelper.Operation op, Date value, Date value2) {
        ColumnStyleColorRangeHelper<ColumnDate> helper = new ColumnStyleColorRangeHelper<>(op, value.getTime(), value2.getTime()+0.0, ()->this);
        colorRangeHelpers.add(helper);
        return helper;
    }
    private CellEditor dateFieldEditor(Object props) {
        return CellEditorFactory.text(props, new DateEditorImpl());
    }
    private final class DateEditorImpl implements CellEditorFactory.CellEditorTextImpl {
        private final HTMLInputElement elem = input("date").element();
        private final DateTimeFormat DTF = format;
        @Override
        public void prepare(Handsontable instance, int row, int col, String prop, HTMLTableCellElement td, String value, Object cell) {
            textHelper.clear(td);
            colorHelper.clear(td);
            for(ColumnStyleColorRangeHelper<ColumnDate> helper: colorRangeHelpers) helper.clear(td);
            alignHelper.clear(td);

            textHelper.apply(td, row, prop, value);
            colorHelper.apply(td, row, prop, value);
            Date parse = null;
            try {parse = format.parse(value);}catch(Exception ignore) {}
            for(ColumnStyleColorRangeHelper<ColumnDate> helper: colorRangeHelpers) helper.apply(td, row, prop, parse!=null?String.valueOf(parse.getTime()):null);
            alignHelper.apply(td, row, prop, value);
        }

        @Override
        public String toValue(String value) {
            if(value == null || value.trim().isEmpty()) return null;
            try {
                return format.format(DTF.parse(value));
            } catch(Exception e) {
                return null;
            }
        }
        @Override
        public void beginEditing(String value, Event evt) {}
        @Override
        public void setValue(String value) {
            if(value == null || value.trim().isEmpty()) elem.value = null;
            else try {
                elem.value = DTF.format((format.parse(value)));
            } catch(Exception e) {
                elem.value = null;
            }
        }
        @Override
        public Element createElement(CellEditorFactory.CellEditorText editorInstance) {
            return elem;
        }
        @Override
        public void init(CellEditorFactory.CellEditorText editorInstance) {}
    }
}
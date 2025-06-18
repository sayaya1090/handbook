package dev.sayaya.handbook.client.interfaces.table.column;

import com.google.gwt.i18n.client.NumberFormat;
import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditor;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditorFactory;
import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableCellElement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.util.LinkedList;
import java.util.List;

import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.span;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Setter
@Accessors(fluent = true)
public final class ColumnNumber implements ColumnBuilder {
    private final static JsRegExp CHK_NUMBER = new JsRegExp("^\\d*(\\.\\d*)?$");
    private final String id;
    private NumberFormat format = NumberFormat.getDecimalFormat();
    @Delegate(excludes = ColumnBuilder.class) private final ColumnBuilderDefaultHelper<ColumnNumber> defaultHelper = new ColumnBuilderDefaultHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleTextHelper<ColumnNumber> textHelper = new ColumnStyleTextHelper<>(()->this);
    private final ColumnStyleDataChangeHelper<ColumnNumber> dataChangeHelper = new ColumnStyleDataChangeHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleColorHelper<ColumnNumber> colorHelper = new ColumnStyleColorHelper<>(()->this);
    private final List<ColumnStyleColorRangeHelper<ColumnNumber>> colorRangeHelpers = new LinkedList<>();
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleAlignHelper<ColumnNumber> alignHelper = new ColumnStyleAlignHelper<>(()->this);
    private final ColumnStyleDataValidateHelper<ColumnNumber> dataValidateHelper = new ColumnStyleDataValidateHelper<>(()->this);
    private final ColumnStyleDataDeleteHelper<ColumnNumber> dataDeleteHelper = new ColumnStyleDataDeleteHelper<>(()->this);
    private static String toString(NumberFormat NF, Object value) throws RuntimeException {
        if(value == null) return null;
        else if(value instanceof Long) return NF.format((Long)value);
        else if(value instanceof Integer) return NF.format((Integer)value);
        else if(value instanceof Double) return NF.format((Double)value);
        else if(value instanceof String cast) {
            var tmp = cast.trim();
            if(tmp.contains(",")) tmp = tmp.replace(",", "");
            RegExpResult chkNumber = CHK_NUMBER.exec(tmp);
            if(chkNumber != null) return tmp;
            else return cast;
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
                    dataDeleteHelper.apply(instance, td, row, prop);
                    for(var helper: colorRangeHelpers) helper.apply(td, row, prop, value);
                    alignHelper.apply(td, row, prop, value);
                    td.innerHTML = toString(format, value);
                    return td;
                }).editor(this::numberFieldEditor)
                .headerRenderer(n->span().text(defaultHelper.name()).element());
    }
    public ColumnStyleColorRangeHelper<ColumnNumber> eq(double value) {
        return range(ColumnStyleColorRangeHelper.Operation.EQ, value);
    }
    public ColumnStyleColorRangeHelper<ColumnNumber> lt(double value) {
        return range(ColumnStyleColorRangeHelper.Operation.LT, value);
    }
    public ColumnStyleColorRangeHelper<ColumnNumber> gt(double value) {
        return range(ColumnStyleColorRangeHelper.Operation.GT, value);
    }
    public ColumnStyleColorRangeHelper<ColumnNumber> le(double value) {
        return range(ColumnStyleColorRangeHelper.Operation.LE, value);
    }
    public ColumnStyleColorRangeHelper<ColumnNumber> ge(double value) {
        return range(ColumnStyleColorRangeHelper.Operation.GE, value);
    }
    public ColumnStyleColorRangeHelper<ColumnNumber> bw(double less, double grater) {
        return range(ColumnStyleColorRangeHelper.Operation.BW, less, grater);
    }
    public ColumnStyleColorRangeHelper<ColumnNumber> bwe(double less, double grater) {
        return range(ColumnStyleColorRangeHelper.Operation.BWE, less, grater);
    }
    private ColumnStyleColorRangeHelper<ColumnNumber> range(ColumnStyleColorRangeHelper.Operation op, double value) {
        ColumnStyleColorRangeHelper<ColumnNumber> helper = new ColumnStyleColorRangeHelper<>(op, value, ()->this);
        colorRangeHelpers.add(helper);
        return helper;
    }
    private ColumnStyleColorRangeHelper<ColumnNumber> range(ColumnStyleColorRangeHelper.Operation op, double value, double value2) {
        ColumnStyleColorRangeHelper<ColumnNumber> helper = new ColumnStyleColorRangeHelper<>(op, value, value2, ()->this);
        colorRangeHelpers.add(helper);
        return helper;
    }
    private CellEditor numberFieldEditor(Object props) {
        return CellEditorFactory.text(props, new NumberEditorImpl());
    }
    private final class NumberEditorImpl implements CellEditorFactory.CellEditorTextImpl {
        private final HTMLInputElement elem = input("number").element();
        @Override
        public void prepare(Handsontable instance, int row, int col, String prop, HTMLTableCellElement td, String value, Object cell) {
            textHelper.clear(td);
            colorHelper.clear(td);
            for(ColumnStyleColorRangeHelper<ColumnNumber> helper: colorRangeHelpers) helper.clear(td);
            alignHelper.clear(td);

            textHelper.apply(td, row, prop, value);
            colorHelper.apply(td, row, prop, value);
            for(ColumnStyleColorRangeHelper<ColumnNumber> helper: colorRangeHelpers) helper.apply(td, row, prop, value);
            alignHelper.apply(td, row, prop, value);
        }

        @Override
        public String toValue(String value) {
            if(value == null || value.trim().isEmpty()) return null;
            try {
                return String.valueOf(format.parse(value));
            } catch(Exception e) {
                return null;
            }
        }
        @Override
        public void beginEditing(String value, Event evt) {}
        @Override
        public void setValue(String value) {
            elem.value = value;
        }
        @Override
        public Element createElement(CellEditorFactory.CellEditorText editorInstance) {
            return elem;
        }
        @Override
        public void init(CellEditorFactory.CellEditorText editorInstance) {}
    }
}

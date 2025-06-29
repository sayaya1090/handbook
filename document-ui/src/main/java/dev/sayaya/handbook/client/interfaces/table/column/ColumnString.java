package dev.sayaya.handbook.client.interfaces.table.column;

import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditor;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditorFactory;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableCellElement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.LinkedList;
import java.util.List;

import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.span;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ColumnString implements ColumnBuilder {
    private final String id;
    @Delegate(excludes = ColumnBuilder.class) private final ColumnBuilderDefaultHelper<ColumnString> defaultHelper = new ColumnBuilderDefaultHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleTextHelper<ColumnString> textHelper = new ColumnStyleTextHelper<>(()->this);
    private final ColumnStyleDataChangeHelper<ColumnString> dataChangeHelper = new ColumnStyleDataChangeHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleColorHelper<ColumnString> colorHelper = new ColumnStyleColorHelper<>(()->this);
    private final List<ColumnStyleColorConditionalHelper<ColumnString>> colorConditionalHelpers = new LinkedList<>();
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleAlignHelper<ColumnString> alignHelper = new ColumnStyleAlignHelper<>(()->this);
    private final ColumnStyleDataValidateHelper<ColumnString> dataValidateHelper = new ColumnStyleDataValidateHelper<>(()->this);
    private final ColumnStyleDataDeleteHelper<ColumnString> dataDeleteHelper = new ColumnStyleDataDeleteHelper<>(()->this);
    @Override
    public Column build() {
        Column column = defaultHelper.build().data(id).header(id);
        return column.renderer((instance, td, row, col, prop, value, ci)->{
                    textHelper.clear(td);
                    colorHelper.clear(td);
                    for (var helper : colorConditionalHelpers) helper.clear(td);
                    alignHelper.clear(td);

                    textHelper.apply(td, row, prop, value);
                    colorHelper.apply(td, row, prop, value);
                    dataChangeHelper.apply(instance, td, row, prop);
                    dataValidateHelper.apply(instance, td, row, prop);
                    dataDeleteHelper.apply(instance, td, row, prop);
                    for (var helper : colorConditionalHelpers) helper.apply(td, row, prop, value);
                    alignHelper.apply(td, row, prop, value);
                    td.innerHTML = value;
                    return td;
                }).editor(this::textFieldEditor)
                .headerRenderer(n->span().text(defaultHelper.name()).element());
    }
    public ColumnStyleColorConditionalHelper<ColumnString> pattern(String pattern) {
        ColumnStyleColorConditionalHelper<ColumnString> helper = new ColumnStyleColorConditionalHelper<>(pattern, ()->this);
        colorConditionalHelpers.add(helper);
        return helper;
    }
    private CellEditor textFieldEditor(Object props) {
        TextEditorImpl impl = new TextEditorImpl();
        return CellEditorFactory.text(props, impl);
    }
    private final class TextEditorImpl implements CellEditorFactory.CellEditorTextImpl {
        private final HTMLInputElement elem = input("text").element();
        @Override
        public void prepare(Handsontable instance, int row, int col, String prop, HTMLTableCellElement td, String value, Object cell) {
            textHelper.clear(td);
            colorHelper.clear(td);
            for (ColumnStyleColorConditionalHelper<ColumnString> helper : colorConditionalHelpers) helper.clear(td);
            alignHelper.clear(td);

            textHelper.apply(td, row, prop, value);
            colorHelper.apply(td, row, prop, value);
            for (ColumnStyleColorConditionalHelper<ColumnString> helper : colorConditionalHelpers) helper.apply(td, row, prop, value);
            alignHelper.apply(td, row, prop, value);
        }
        @Override
        public String toValue(String value) {
            return value;
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

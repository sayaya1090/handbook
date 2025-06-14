package dev.sayaya.handbook.client.interfaces.table.column;

import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.ui.elements.CheckboxElementBuilder;
import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;
import lombok.experimental.Delegate;

import java.util.LinkedList;
import java.util.List;

import static org.jboss.elemento.Elements.span;

public class ColumnCheckBox implements ColumnBuilder {
    private final static JsRegExp CHK_BOOLEAN = new JsRegExp("^true|false$");
    private static String normalize(String str) {
        if(str == null) return null;
        str = str.trim().toLowerCase();
        RegExpResult chkBool = CHK_BOOLEAN.exec(str);
        if(chkBool != null) return str;
        else return "false";
    }
    private final String id;
    @Delegate(excludes = ColumnBuilder.class) private final ColumnBuilderDefaultHelper<ColumnCheckBox> defaultHelper = new ColumnBuilderDefaultHelper<>(()->this);
    private final ColumnStyleDataChangeHelper<ColumnCheckBox> dataChangeHelper = new ColumnStyleDataChangeHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleColorHelper<ColumnCheckBox> colorHelper = new ColumnStyleColorHelper<>(()->this);
    private final List<ColumnStyleColorConditionalHelper<ColumnCheckBox>> colorConditionalHelpers = new LinkedList<>();
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleAlignHelper<ColumnCheckBox> alignHelper = new ColumnStyleAlignHelper<>(()->this);
    private final ColumnStyleDataValidateHelper<ColumnCheckBox> dataValidateHelper = new ColumnStyleDataValidateHelper<>(()->this);
    private final ColumnStyleDataDeleteHelper<ColumnCheckBox> dataDeleteHelper = new ColumnStyleDataDeleteHelper<>(()->this);
    ColumnCheckBox(String id) {
        this.id = id;
        alignHelper.horizontal("center");
    }
    @Override
    public Column build() {
        Column column = defaultHelper.build().data(id).header(id).readOnly(true);
        return column.renderer((instance, td, row, col, prop, value, ci)->{
            Data data = instance.getSettings().data[row];
            value = normalize(value);
            alignHelper.clear(td);
            colorHelper.clear(td);
            for(var helper: colorConditionalHelpers) helper.clear(td);
            alignHelper.apply(td, row, prop, value);
            colorHelper.apply(td, row, prop, value);
            dataChangeHelper.apply(instance, td, row, prop);
            dataValidateHelper.apply(instance, td, row, prop);
            dataDeleteHelper.apply(instance, td, row, prop);
            for(var helper: colorConditionalHelpers) helper.apply(td, row, prop, value==null?"false":value);


            var elem = CheckboxElementBuilder.checkbox().select(Boolean.parseBoolean(value)).style("vertical-align: sub; --md-checkbox-outline-width: 1px;");
            if(defaultHelper.readOnly() || dataDeleteHelper.deleted(data)) elem.element().setAttribute("disabled", "true");
            else if(data!=null) elem.onChange(evt->{
                data.put(id, evt!=null?String.valueOf(elem.isSelected()):"false");
                String v = normalize(String.valueOf(elem.isSelected()));
                colorHelper.clear(td);
                for(var helper: colorConditionalHelpers) helper.clear(td);

                colorHelper.apply(td, row, prop, v);
                dataChangeHelper.apply(instance, td, row, prop);
                dataValidateHelper.apply(instance, td, row, prop);
                dataDeleteHelper.apply(instance, td, row, prop);
                for(var helper: colorConditionalHelpers) helper.apply(td, row, prop, v==null?"false":v);
            });
            td.innerHTML = "";
            td.appendChild(elem.element());
            return td;
        }).headerRenderer(n->span().text(defaultHelper.name()).element());
    }
    public ColumnStyleColorConditionalHelper<ColumnCheckBox> isTrue() {
        ColumnStyleColorConditionalHelper<ColumnCheckBox> helper = new ColumnStyleColorConditionalHelper<>("true", ()->this);
        colorConditionalHelpers.add(helper);
        return helper;
    }
    public ColumnStyleColorConditionalHelper<ColumnCheckBox> isFalse() {
        ColumnStyleColorConditionalHelper<ColumnCheckBox> helper = new ColumnStyleColorConditionalHelper<>("false", () -> this);
        colorConditionalHelpers.add(helper);
        return helper;
    }
}
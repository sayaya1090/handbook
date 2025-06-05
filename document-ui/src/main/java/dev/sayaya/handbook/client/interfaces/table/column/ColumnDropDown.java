package dev.sayaya.handbook.client.interfaces.table.column;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.regexp.shared.RegExp;
import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.*;
import lombok.experimental.Delegate;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

public final class ColumnDropDown implements ColumnBuilder {
    private final String id;
    private final String[] list;
    @Delegate(excludes = ColumnBuilder.class) private final ColumnBuilderDefaultHelper<ColumnDropDown> defaultHelper = new ColumnBuilderDefaultHelper<>(()->this);
    private final ColumnStyleDataChangeHelper<ColumnDropDown> dataChangeHelper = new ColumnStyleDataChangeHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnDropDownStyleColorHelper colorHelper = new ColumnDropDownStyleColorHelper(()->this) ;
    private final List<ColumnDropDownStyleColorConditionalHelper> colorConditionalHelpers = new LinkedList<>();
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleAlignHelper<ColumnDropDown> alignHelper = new ColumnStyleAlignHelper<>(()->this);
    ColumnDropDown(String id, String... list) {
        this.id = id;
        this.list = list;
    }
    @Override
    public Column build() {
        Column column = defaultHelper.build().data(id).header(id);
        return column.readOnly(true).renderer((sheet, td, row, col, prop, value, ci)->{
            Data data = sheet.getSettings().data[row];
            var elem = SelectElementBuilder.select().outlined().option().value(null).headline("").end();
            alignHelper.clear(td);
            colorHelper.clear(td);
            for(var helper: colorConditionalHelpers) helper.clear(td);
            alignHelper.apply(td, row, prop, value);
            colorHelper.apply(td, row, prop, value);
            dataChangeHelper.apply(sheet, td, row, prop);
            for(var helper: colorConditionalHelpers) helper.apply(td, row, prop, value);
            Scheduler.get().scheduleDeferred(()->{
                var label = elem.element().shadowRoot.getElementById("label");
                label.style.paddingTop = CSSProperties.PaddingTopUnionType.of("0px");
                label.style.paddingBottom = CSSProperties.PaddingBottomUnionType.of("0px");
                label.style.fontSize = CSSProperties.FontSizeUnionType.of("10px");
                label.style.lineHeight = CSSProperties.LineHeightUnionType.of("20px");

                var field = elem.element().shadowRoot.getElementById("field");
                field.style.height = CSSProperties.HeightUnionType.of("20px");
                field.style.setProperty("--_outline-width",  "0px");
                field.style.setProperty("--_hover-outline-width",  "0px");
                field.style.setProperty("--_focus-outline-width",  "0px");
                field.style.setProperty("--_disabled-outline-width",  "0px");
            });
            for(String option: list) elem.option().value(option).headline(option).select(option.equals(value));
            if(defaultHelper.readOnly()) elem.enable(false);
            else if(data!=null) elem.onChange(evt->{
                String v = elem.value();
                data.put(id, v);
                colorHelper.clear(td);
                for(var helper: colorConditionalHelpers) helper.clear(td);

                colorHelper.apply(td, row, prop, v);
                dataChangeHelper.apply(sheet, td, row, prop);
                for(var helper: colorConditionalHelpers) helper.apply(td, row, prop, v);
            });
            td.innerHTML = "";
            td.style.padding = CSSProperties.PaddingUnionType.of("0");
            td.style.verticalAlign = "middle";
            td.appendChild(div().style("white-space", "initial")
                    .style("height", "20px")
                    .add(elem.style("width", "100%")).element());
            return td;
        }).headerRenderer(n->span().text(defaultHelper.name()).element());
    }
    public ColumnDropDownStyleColorConditionalHelper pattern(String pattern) {
        ColumnDropDownStyleColorConditionalHelper helper = new ColumnDropDownStyleColorConditionalHelper(pattern, () -> this);
        colorConditionalHelpers.add(helper);
        return helper;
    }
    private final static class ColumnDropDownStyleColorHelper implements ColumnStyleHelper<ColumnDropDown> {
        private final Supplier<ColumnDropDown> _self;
        private ColumnStyleFn<String> color;
        private ColumnStyleFn<String> colorBackground;
        public ColumnDropDownStyleColorHelper(Supplier<ColumnDropDown> columnBuilder) {
            _self = columnBuilder;
        }
        @Override
        public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
            if(color!=null) {
                td.style.setProperty("--md-outlined-select-text-field-focus-input-text-color", color.apply(td, row, prop, value));
                td.style.setProperty("--md-outlined-select-text-field-hover-input-text-color", color.apply(td, row, prop, value));
                td.style.setProperty("--md-outlined-select-text-field-input-text-color", color.apply(td, row, prop, value));
            }
            if(colorBackground!=null)   td.style.backgroundColor    = colorBackground.apply(td, row, prop, value);
            return td;
        }
        @Override
        public ColumnDropDown clear(HTMLElement td) {
            td.style.removeProperty("--md-outlined-select-text-field-focus-input-text-color");
            td.style.removeProperty("--md-outlined-select-text-field-hover-input-text-color");
            td.style.removeProperty("--md-outlined-select-text-field-input-text-color");
            td.style.removeProperty("background-color");
            return that();
        }
        public ColumnDropDown color(String color) {
            if(color == null) return color((ColumnStyleFn<String>)null);
            return color((td, row, prop, value)->color);
        }
        public ColumnDropDown color(ColumnStyleFn<String> color) {
            this.color = color;
            return that();
        }
        public ColumnDropDown colorBackground(String colorBackground) {
            if(colorBackground == null) return colorBackground((ColumnStyleFn<String>)null);
            return colorBackground((td, row, prop, value)->colorBackground);
        }
        public ColumnDropDown colorBackground(ColumnStyleFn<String> colorBackground) {
            this.colorBackground = colorBackground;
            return that();
        }
        private ColumnDropDown that() {
            return _self.get();
        }
    }
    public static final class ColumnDropDownStyleColorConditionalHelper implements ColumnStyleHelper<ColumnDropDown> {
        private final RegExp pattern;
        private ColumnStyleFn<String> color;
        private ColumnStyleFn<String> colorBackground;
        private final Supplier<ColumnDropDown> _self;
        public ColumnDropDownStyleColorConditionalHelper(String pattern, Supplier<ColumnDropDown> columnBuilder) {
            this.pattern = RegExp.compile(pattern.trim());
            _self = columnBuilder;
        }
        @Override
        public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
            if(value == null) return td;
            if(pattern.test(value.trim())) {
                if(color !=null) {
                    td.style.setProperty("--md-outlined-select-text-field-focus-input-text-color", color.apply(td, row, prop, value));
                    td.style.setProperty("--md-outlined-select-text-field-hover-input-text-color", color.apply(td, row, prop, value));
                    td.style.setProperty("--md-outlined-select-text-field-input-text-color", color.apply(td, row, prop, value));
                }
                if(colorBackground !=null)   td.style.backgroundColor    = colorBackground.apply(td, row, prop, value);
            }
            return td;
        }
        @Override
        public ColumnDropDown clear(HTMLElement td) {
            td.style.removeProperty("--md-outlined-select-text-field-focus-input-text-color");
            td.style.removeProperty("--md-outlined-select-text-field-hover-input-text-color");
            td.style.removeProperty("--md-outlined-select-text-field-input-text-color");
            td.style.removeProperty("background-color");
            return that();
        }
        public ColumnDropDown than(String color) {
            if(color == null) return than((ColumnStyleFn<String>)null);
            return than((td, row, prop, value)->color);
        }
        public ColumnDropDown than(ColumnStyleFn<String> color) {
            this.color = color;
            return that();
        }
        public ColumnDropDown than(String color, String background) {
            if(background == null) return than(color);
            if(color == null) return than(null, (td, row, prop, value)->background);
            return than((td, row, prop, value)->color, (td, row, prop, value)->background);
        }
        public ColumnDropDown than(ColumnStyleFn<String> color, ColumnStyleFn<String> background) {
            this.color = color;
            this.colorBackground = background;
            return that();
        }
        private ColumnDropDown that() {
            return _self.get();
        }
    }
}

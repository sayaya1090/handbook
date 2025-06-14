package dev.sayaya.handbook.client.interfaces.table.column;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.regexp.shared.RegExp;
import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Data;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditor;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditorFactory;
import dev.sayaya.ui.elements.SelectElementBuilder;
import elemental2.dom.*;
import lombok.experimental.Delegate;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static org.jboss.elemento.Elements.*;

public final class ColumnDropDown implements ColumnBuilder {
    private final String id;
    private final String[] list;
    @Delegate(excludes = ColumnBuilder.class) private final ColumnBuilderDefaultHelper<ColumnDropDown> defaultHelper = new ColumnBuilderDefaultHelper<>(()->this);
    private final ColumnStyleDataChangeHelper<ColumnDropDown> dataChangeHelper = new ColumnStyleDataChangeHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnDropDownStyleColorHelper colorHelper = new ColumnDropDownStyleColorHelper(()->this) ;
    private final List<ColumnDropDownStyleColorConditionalHelper> colorConditionalHelpers = new LinkedList<>();
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleAlignHelper<ColumnDropDown> alignHelper = new ColumnStyleAlignHelper<>(()->this);
    private final ColumnStyleDataValidateHelper<ColumnDropDown> dataValidateHelper = new ColumnStyleDataValidateHelper<>(()->this);
    private final ColumnStyleDataDeleteHelper<ColumnDropDown> dataDeleteHelper = new ColumnStyleDataDeleteHelper<>(()->this);
    ColumnDropDown(String id, String... list) {
        this.id = id;
        this.list = list;
    }
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleTextHelper<ColumnDropDown> textHelper = new ColumnStyleTextHelper<>(()->this);
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
                }).editor(this::selectEditor)
                .headerRenderer(n->span().text(defaultHelper.name()).element());
    }
    private CellEditor selectEditor(Object props) {
        var impl = new SelectEditorImpl();
        return CellEditorFactory.base(props, impl);
    }
    private final class SelectEditorImpl implements CellEditorFactory.CellEditorBaseImpl {
        private final SelectElementBuilder<?, ?> input = SelectElementBuilder.select()
                .outlined().option().value(null).headline("").end()
                .style("position: absolute; min-width: auto; width: 100%; overflow: hidden;");
        private final List<SelectElementBuilder.SelectOptionElementBuilder<?>> options = new LinkedList<>();
        private CellEditorFactory.CellEditorBase editorInstance; // Handsontable 에디터 인스턴스 저장용
        private boolean isViewInitialized = false; // 스타일 적용이 한 번만 되도록 하기 위한 플래그
        SelectEditorImpl() {
            for(String option: list) options.add(input.option().value(option).headline(option));
        }
        @Override
        public Element getElement() {
            return input.element();
        }
        private boolean opened = false;
        @Override
        public void init(CellEditorFactory.CellEditorBase editorInstance) {
            this.editorInstance = editorInstance;
            input.onChange(evt -> this.editorInstance.finishEditing());
            input.element().addEventListener("opening", evt->opened = true);
            input.element().addEventListener("closed", evt->opened = false);
        }
        @Override // 편집이 시작될 때 호출됩니다.
        public void open(CellEditorFactory.CellEditorBase editorInstance, Event event) {
            if (event instanceof KeyboardEvent keyboardEvent) {
                var keyPressed = keyboardEvent.key.toLowerCase();
                for (var opt : options) if (opt.element().value.toLowerCase().startsWith(keyPressed)) {
                    setValue(opt.element().value);
                    break;
                }
            }
            if (!isViewInitialized) Scheduler.get().scheduleDeferred(() -> {
                var label = input.element().shadowRoot.getElementById("label");
                label.style.setProperty("--_top-space", "0px");
                label.style.setProperty("--_bottom-space", "0px");
                label.style.setProperty("--_leading-space", "0px");
                label.style.setProperty("--_outline-label-padding", "0px");
                label.style.setProperty("color", "transparent");

                var field = input.element().shadowRoot.getElementById("field");
                field.style.height = CSSProperties.HeightUnionType.of("20px");
                field.style.setProperty("--_outline-width", "0px");
                field.style.setProperty("--_hover-outline-width", "0px");
                field.style.setProperty("--_focus-outline-width", "0px");
                field.style.setProperty("--_disabled-outline-width", "0px");
                var iconSlot = (HTMLElement) field.shadowRoot.querySelector(".end");
                iconSlot.style.setProperty("min-width", "0px");
                isViewInitialized = true;
            });
            Scheduler.get().scheduleDeferred(() -> {
                if(!opened) input.element().click();
            });
        }
        @Override
        public void setValue(String stringfiedInitialValue) {
            for(var opt: options) if(opt.element().value.equals(stringfiedInitialValue)) {
                if(!opt.isSelected()) opt.select(true);
                break;
            }
        }
        @Override public void focus() {}
        @Override public void close() {}
        @Override
        public void prepare(Handsontable instance, int row, int col, String prop, HTMLTableCellElement td, String value, Object cell) {
            alignHelper.clear(td);
            colorHelper.clear(td);
            for(var helper: colorConditionalHelpers) helper.clear(td);
            alignHelper.apply(td, row, prop, value);
            colorHelper.apply(td, row, prop, value);
            dataChangeHelper.apply(instance, td, row, prop);
            dataValidateHelper.apply(instance, td, row, prop);
            dataDeleteHelper.apply(instance, td, row, prop);
            for(var helper: colorConditionalHelpers) helper.apply(td, row, prop, value);
            if(defaultHelper.readOnly()) input.enable(false);
        }
        @Override
        public String getValue() {
            return input.value();
        }
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

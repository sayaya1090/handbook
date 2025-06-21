package dev.sayaya.handbook.client.interfaces.table.column;

import com.google.gwt.regexp.shared.RegExp;
import dev.sayaya.handbook.client.interfaces.table.Column;
import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditor;
import dev.sayaya.handbook.client.interfaces.table.function.CellEditorFactory;
import dev.sayaya.ui.dom.MdChipElement;
import elemental2.dom.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.HTMLInputElementBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.elemento.Elements.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Setter
@Accessors(fluent = true)
public class ColumnChip implements ColumnBuilder {
    private final static String SPLITTER = "(?<!\\\\),";
    private final String id;
    @Delegate(excludes = ColumnBuilder.class) private final ColumnBuilderDefaultHelper<ColumnChip> defaultHelper = new ColumnBuilderDefaultHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleTextHelper<ColumnChip> textHelper = new ColumnStyleTextHelper<>(()->this);
    private final ColumnStyleDataChangeHelper<ColumnChip> dataChangeHelper = new ColumnStyleDataChangeHelper<>(()->this);
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleColorHelper<ColumnChip> colorHelper = new ColumnStyleColorHelper<>(()->this);
    private final List<ChipStyleColorConditionalHelper<ColumnChip>> colorConditionalHelpers = new LinkedList<>();
    @Delegate(excludes = ColumnStyleHelper.class) private final ColumnStyleFlexAlignHelper<ColumnChip> alignHelper = new ColumnStyleFlexAlignHelper<>(()->this);
    private final ColumnStyleDataValidateHelper<ColumnChip> dataValidateHelper = new ColumnStyleDataValidateHelper<>(()->this);
    private final ColumnStyleDataDeleteHelper<ColumnChip> dataDeleteHelper = new ColumnStyleDataDeleteHelper<>(()->this);
    @Override public Column build() {
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
                    if(value==null) value = "";
                    td.innerHTML = value.replace("\\,", "&&&&").replace(" ", "&nbsp;").replace(",", "<br/>").replace("&&&&", ",");
                    return td;
                }).editor(this::selectEditor)
                .headerRenderer(n->span().text(defaultHelper.name()).element());
    }
    private CellEditor selectEditor(Object props) {
        var impl = new ChipEditorImpl();
        return CellEditorFactory.base(props, impl);
    }
    private final class ChipEditorImpl implements CellEditorFactory.CellEditorBaseImpl {
        private final HTMLInputElementBuilder<HTMLInputElement> input = input("text").style("background: transparent; border: none; outline: none; min-width: 1rem;");
        private final HTMLContainerBuilder<HTMLDivElement> container = div().style("background: var(--md-sys-color-primary-container); display: " +
                "flex; flex-wrap: wrap; overflow: hidden auto; flex-direction: row; gap: 0.5rem; height: 100%; " +
                "border: 1px solid var(--md-sys-color-primary);");
        private CellEditorFactory.CellEditorBase editorInstance; // Handsontable 에디터 인스턴스 저장용
        private String value;
        ChipEditorImpl() {}
        @Override public Element getElement() {
            return container.element();
        }
        @Override public void init(CellEditorFactory.CellEditorBase editorInstance) {
            this.editorInstance = editorInstance;
            input.on(EventType.change, evt -> this.editorInstance.finishEditing());
        }
        @Override // 편집이 시작될 때 호출됩니다.
        public void open(CellEditorFactory.CellEditorBase editorInstance, Event event) {

        }
        @Override
        public void setValue(String value) {
            if(value==null) value = "";
            this.value = value;
            var match = value.split(SPLITTER);
            var tokens = Arrays.stream(match).filter(k->k!=null && !k.isEmpty()).map(text->{
                if(text.contains("\\,")) return text.replace("\\,", ",");
                return text;
            });
            container.element().innerHTML = "";
            if (readOnly()) tokens.map(token-> {
                var chip = htmlContainer("md-assist-chip", MdChipElement.MdAssistChipElement.class);
                chip.element().label = token;
                return chip;
            })/*.peek(e->style(e.element(), row, prop))*/.forEach(container::add);
            else {
                tokens.map(token->{
                    var chip = htmlContainer("md-input-chip", MdChipElement.MdInputChipElement.class);
                    chip.element().label = token;
                    return chip;
                })/*.peek(e->style(e.element(), row, prop))*/.forEach(container::add);
            }
            container.add(input);
        }
        private void style(MdChipElement.MdAssistChipElement chip, int row, String prop) {
            String value = chip.label;
            for(ChipStyleColorConditionalHelper<?> helper: colorConditionalHelpers) {
                helper.apply(chip, row, prop, value);
            }
        }
        private void style(MdChipElement.MdInputChipElement chip, int row, String prop) {
            String value = chip.label;
            for (ChipStyleColorConditionalHelper<?> helper : colorConditionalHelpers) {
                helper.apply(chip, row, prop, value);
            }
        }
        @Override public void focus() {
            input.element().focus();
        }
        @Override public void close() {
            input.value("");
        }
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
            //if(defaultHelper.readOnly()) container.enable(false);
        }
        @Override
        public String getValue() {
            var match = value.split(SPLITTER);
            var text = input.element().value;
            if(text.contains(",")) text = text.replace(",", "\\,");
            return Stream.concat(Arrays.stream(match), Stream.of(text)).filter(str->str!=null && !str.isEmpty()).collect(Collectors.joining(","));
        }
    }
    /*
    @Override
    public Column build() {
        Column column = defaultHelper.build().data(id).header(id);
        return column.readOnly(true).renderer((sheet, td, row, col, prop, value, ci)->{
            alignHelper.clear(td);
            colorHelper.clear(td);
            for(ChipStyleColorConditionalHelper<?> helper: colorConditionalHelpers) helper.clear(td);
            alignHelper.apply(td, row, prop, value);
            colorHelper.apply(td, row, prop, value);
            dataChangeHelper.apply(sheet, td, row, prop);
            var elem = div().style("display: flex; flex-wrap: wrap; overflow: hidden auto; flex-direction: row; gap: 0.5rem; margin: 4px;");
            if(value==null) value = "";
            var match = value.split(SPLITTER);
            var tokens = Arrays.stream(match).filter(k->k!=null && !k.isEmpty()).map(text->{
                if(text.contains("\\,")) return text.replace("\\,", ",");
                return text;
            });
            if (readOnly()) tokens.map(token-> {
                var chip = htmlContainer("md-assist-chip", MdChipElement.MdAssistChipElement.class);
                chip.element().label = token;
                return chip;
            }).peek(e->style(e.element(), row, prop)).forEach(elem::add);
            else {
                tokens.map(token->{
                    var chip = htmlContainer("md-input-chip", MdChipElement.MdInputChipElement.class);
                    chip.element().label = token;
                    return chip;
                }).peek(e->style(e.element(), row, prop)).forEach(c->elem.add(c));
                var input = input("text").style("background: transparent; border: none; outline: none; min-width: 1rem;");
                input.on(EventType.click, evt->input.element().focus());
                input.on(EventType.change, evt->{
                    var text = input.element().value;
                    if(text.contains(",")) text = text.replace(",", "\\,");
                    String nextValue = Stream.concat(Arrays.stream(match), Stream.of(text)).collect(Collectors.joining(","));
                    sheet.setDataAtCell(row, col, nextValue);
                });
                elem.add(input);
            }
            td.innerHTML = "";
            td.append(elem.element());
            Scheduler.get().scheduleDeferred(()->{
               elem.element().style.height = CSSProperties.HeightUnionType.of(elem.element().offsetHeight + "px");
            });
            alignHelper.apply(td, row, prop, value);
            return td;
        }).headerRenderer(n->span().text(defaultHelper.name()).element());
    }*/

    /*
    public ChipStyleColorConditionalHelper<ColumnChip> pattern(String pattern) {
        var helper = new ChipStyleColorConditionalHelper<>(pattern, ()->this);
        colorConditionalHelpers.add(helper);
        return helper;
    }*/
    public final static class ColumnStyleFlexAlignHelper<SELF> implements ColumnStyleHelper<SELF> {
        private final Supplier<SELF> _self;
        private String horizontal;
        private String vertical;
        public ColumnStyleFlexAlignHelper(Supplier<SELF> columnBuilder) {
            _self = columnBuilder;
        }
        @Override
        public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
            var children = td.getElementsByTagName("div").asList();
            if(children.size()<=0) return td;
            var flex = (HTMLElement) children.get(0);
            if(horizontal !=null)	flex.style.justifyContent = horizontal;
            if(vertical!=null)  	flex.style.alignContent   = vertical;
            return td;
        }
        @Override
        public SELF clear(HTMLElement td) {
            var children = td.getElementsByTagName("div").asList();
            if(children.size()<=0) return that();
            var flex = (HTMLElement) children.get(0);
            flex.style.removeProperty("justify-content");
            flex.style.removeProperty("align-content");
            return that();
        }
        public SELF horizontal(String horizontal) {
            if("left".equalsIgnoreCase(vertical)) vertical = "flex-start";
            if("right".equalsIgnoreCase(vertical)) vertical = "flex-end";
            this.horizontal = horizontal;
            return that();
        }
        public SELF vertical(String vertical) {
            if("top".equalsIgnoreCase(vertical)) vertical = "flex-start";
            if("bottom".equalsIgnoreCase(vertical)) vertical = "flex-end";
            this.vertical = vertical;
            return that();
        }
        private SELF that() {
            return _self.get();
        }
    }
    public final static class ChipStyleColorConditionalHelper<SELF> implements ColumnStyleHelper<SELF> {
        private final RegExp pattern;
        private ColumnStyleFn<String> color;
        private ColumnStyleFn<String> colorBackground;
        private final Supplier<SELF> _self;
        public ChipStyleColorConditionalHelper(String pattern, Supplier<SELF> columnBuilder) {
            this.pattern = RegExp.compile(pattern.trim());
            _self = columnBuilder;
        }
        @Override
        public HTMLElement apply(HTMLElement td, int row, String prop, String value) {
            if(value == null) return td;
            if(pattern.test(value.trim())) {
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
}
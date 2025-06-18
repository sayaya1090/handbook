package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.domain.AttributeTypeDefinition;
import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.domain.validator.*;
import dev.sayaya.handbook.client.interfaces.table.column.ColumnBuilder;
import dev.sayaya.handbook.client.usecase.ActionManager;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.elements.CheckboxElementBuilder;
import elemental2.core.JsArray;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.KeyboardEvent;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;
import java.util.stream.Stream;

import static dev.sayaya.ui.elements.CheckboxElementBuilder.checkbox;
import static org.jboss.elemento.Elements.label;

@Singleton
public class DocumentTableElement implements IsElement<HTMLDivElement> {
    private static final String KEY_Z = "KeyZ";
    private final CheckboxElementBuilder checkAll = checkbox().style("vertical-align: bottom; --md-checkbox-outline-width: 1px;");
    private final Map<Integer, CheckboxElementBuilder> rowHeaders = new HashMap<>();
    private final HandsontableConfiguration config = new HandsontableConfiguration();
    private final HandsontableElement table;
    private String lblSerial = "Serial";
    private String lblEffectDatetime = "Effect date";
    private String lblExpireDatetime = "Expire date";
    private final TypeProvider typeProvider;
    private final ActionManager actionManager;
    @Inject DocumentTableElement(TypeProvider type, DataProvider data, ActionManager actionManager, Observable<Label> labels) {
        this.typeProvider = type;
        this.actionManager = actionManager;
        setting();
        table = new HandsontableElement(config);
        type.subscribe(this::update);
        data.subscribe(this::update);
        labels.subscribe(this::update);
        initEventHandlers();
    }
    private void update(Label label) {
        if (label == null) return;
        lblSerial = Label.findLabelOrDefault(label, "Serial");
        lblEffectDatetime = Label.findLabelOrDefault(label, "Effect date");
        lblExpireDatetime = Label.findLabelOrDefault(label, "Expire date");
        update(typeProvider.getValue());
    }
    private void update(Type type) {
        if(type==null || type.attributes().isEmpty()) return;
        config.columns = Stream.concat(
                        Stream.of(
                                ColumnBuilder.string("Serial")
                                        .pattern("^\\s*$")
                                        .than((t, r, p, v)->"transparent",
                                              (t, r, p, v)->"var(--md-sys-color-error)")
                                        .horizontal("center").vertical("middle")
                                        .build().header(lblSerial),
                                ColumnBuilder.string("Effect date time").horizontal("center").vertical("middle").build().header(lblEffectDatetime),
                                ColumnBuilder.string("Expire date time").horizontal("center").vertical("middle").build().header(lblExpireDatetime)
                        ), type.attributes().stream().map(this::toColumn).map(ColumnBuilder::build)
                ).toArray(Column[]::new);
        config.cells = (row, col, prop) -> {
            var cellProperties = Js.asPropertyMap(new Object());
            Data data = config.data[row];
            if(data == null) return cellProperties;
            var isDeleted = "DELETE".equals(data.get("$state"));
            if(isDeleted) cellProperties.set("readOnly", true);
            return cellProperties;
        };
        table.updateSettings(config);
    }
    private ColumnBuilder toColumn(Attribute attr) {
        if(attr.type().baseType() == AttributeTypeDefinition.AttributeType.Value) {
            var validators = attr.type().validators();
            if(validators.isEmpty()) return ColumnBuilder.string(attr.name()).horizontal("center").vertical("middle");
            else if(validators.get(0) instanceof ValidatorRegex validator) {
                var builder = ColumnBuilder.string(attr.name()).horizontal("center").vertical("middle");
                builder.pattern(validator.pattern());
                return builder;
            } else if(validators.get(0) instanceof ValidatorBool) {
                return ColumnBuilder.checkbox(attr.name()).horizontal("center").vertical("middle");
            } else if(validators.get(0) instanceof ValidatorNumber) {
                return ColumnBuilder.number(attr.name()).horizontal("center").vertical("middle");
            } else if(validators.get(0) instanceof ValidatorDate) {
                return ColumnBuilder.date(attr.name()).horizontal("center").vertical("middle").width(200);
            } else if(validators.get(0) instanceof ValidatorEnum validator) {
                return ColumnBuilder.dropdown(attr.name(), validator.options()).horizontal("center").vertical("middle").width(200);
            }
        }
        return ColumnBuilder.string(attr.name());
    }
    private void update(List<Data> data) {
        checkAll.select(false);
        for(var checkbox: rowHeaders.values()) checkbox.element().remove();
        rowHeaders.clear();
        config.data = data.stream().toArray(Data[]::new);
        table.updateSettings(config);
    }
    private void setting() {
        config.stretchH = "all";
        config.rowHeaders = false;
        config.width = "100%";
        config.rowHeaderWidth = 30;
        config.data = new Data[0];
        selectAllHeader();
        selectRowHeader();
    }
    private void selectRowHeader() {
        config.afterGetRowHeaderRenderers = renderers -> {
            JsArray.asJsArray(renderers).push((row, th)->{
                var header = rowHeaders.computeIfAbsent(row, r->{
                    var checkbox = checkbox().style("vertical-align: sub; --md-checkbox-outline-width: 1px;");
                    if(config.data.length > row) {
                        config.data[row].onStateChange(state->checkbox.select(state.state() == Data.DataState.SELECTED));
                        checkbox.onChange(evt->config.data[row].select(checkbox.isSelected()));
                    }
                    return checkbox;
                });
                th.append(header.element());
            });
        };
    }
    private void selectAllHeader() {
        config.afterGetColumnHeaderRenderers = renderers -> {
            var defaultRenderer = renderers[0];
            renderers[0] = (col, th) -> {
                if(col == -1) th.append(checkAll.element());
                else if(defaultRenderer!=null) defaultRenderer.accept(col, th);
                else th.innerHTML = label(config.columns[col].header).element().outerHTML;
                return th;
            };
        };
        checkAll.onChange(evt->{
            for (int i = 0; i < config.data.length; i++) config.data[i].select(checkAll.isSelected());
            if(checkAll.isSelected()) table.selectRows(0, config.data.length-1);
            else table.deselectCell();
        });
    }
    private void initEventHandlers() {
        table.on(EventType.keydown, this::handleKeyPress);
    }
    private void handleKeyPress(KeyboardEvent evt) {
        if(evt.ctrlKey) {
            if(evt.code.equals(KEY_Z)) {
                if (evt.shiftKey) actionManager.redo();
                else actionManager.undo();
                element().focus();
            }
        }
    }
    @Override
    public HTMLDivElement element() {
        return table.element();
    }
}

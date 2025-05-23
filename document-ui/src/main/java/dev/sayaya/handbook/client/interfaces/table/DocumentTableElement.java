package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.interfaces.table.column.ColumnBuilder;
import dev.sayaya.handbook.client.interfaces.table.column.ColumnString;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.elements.CheckboxElementBuilder;
import elemental2.core.JsArray;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static dev.sayaya.ui.elements.CheckboxElementBuilder.checkbox;
import static org.jboss.elemento.Elements.label;

@Singleton
public class DocumentTableElement implements IsElement<HTMLDivElement> {
    private final CheckboxElementBuilder checkAll = checkbox().style("vertical-align: bottom; --md-checkbox-outline-width: 1px;");
    private final Map<Integer, CheckboxElementBuilder> rowHeaders = new HashMap<>();
    private final HandsontableConfiguration config = new HandsontableConfiguration();
    private final HandsontableElement table;
    private String lblSerial = "Serial";
    private String lblEffectDatetime = "Effect date";
    private String lblExpireDatetime = "Expire date";
    private final TypeProvider typeProvider;
    @Inject DocumentTableElement(TypeProvider type, DataProvider data, Observable<Label> labels) {
        this.typeProvider = type;
        setting();
        table = new HandsontableElement(config);
        type.subscribe(this::update);
        data.subscribe(this::update);
        labels.subscribe(this::update);
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
                                ColumnBuilder.string("Serial").build().header(lblSerial),
                                ColumnBuilder.string("Effect date time").build().header(lblEffectDatetime),
                                ColumnBuilder.string("Expire date time").build().header(lblExpireDatetime)
                        ), type.attributes().stream().map(attr-> ColumnBuilder.string(attr.name())).map(ColumnString::build)
                ).toArray(Column[]::new);
        table.updateSettings(config);
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
                    var checkbox = checkbox().style("vertical-align: bottom; --md-checkbox-outline-width: 1px;");
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
    @Override
    public HTMLDivElement element() {
        return table.element();
    }
}

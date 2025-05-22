package dev.sayaya.handbook.client.interfaces.table;

import com.google.gwt.aria.client.State;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.interfaces.table.column.ColumnBuilder;
import dev.sayaya.handbook.client.interfaces.table.column.ColumnString;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.ui.elements.CheckboxElementBuilder;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.sayaya.ui.elements.CheckboxElementBuilder.checkbox;
import static org.jboss.elemento.Elements.label;

@Singleton
public class DocumentTableElement implements IsElement<HTMLDivElement> {
    private final CheckboxElementBuilder checkAll = checkbox().style("vertical-align: middle; --md-checkbox-outline-width: 1px;");
    private final Map<Integer, CheckboxElementBuilder> rowHeaders = new HashMap<>();
    private final HandsontableConfiguration config = new HandsontableConfiguration();
    private final HandsontableElement table;
    @Inject DocumentTableElement(TypeProvider type, DataProvider data) {
        setting();
        table = new HandsontableElement(config);
        type.subscribe(this::update);
        data.subscribe(this::update);
    }
    private void update(Type type) {
        if(type==null || type.attributes().isEmpty()) return;
        config.columns = type.attributes().stream()
                .map(attr-> ColumnBuilder.string(attr.name()))
                .map(ColumnString::build)
                .toArray(Column[]::new);
        table.updateSettings(config);
       /*var column = config.columns.computeIfAbsent(attr.name(), name->{
                var column = new Column();
                column.name(name);
                column.type(attr.type());
                column.readOnly(attr.readOnly());
                return column;
            });
            if(attr.type() == Type.TypeAttribute.TypeAttributeType.CHECKBOX) {
                column.renderer((row, col, value, cell, config) -> {
                    cell.textContent = null;
                    cell.appendChild(rowHeaders.get(row).element());
                });
            } else {
                column.renderer((row, col, value, cell, config) -> {
                    cell.textContent = value.toString();
                });
            }*/
    }
    private void update(List<Data> data) {
        config.data = data.stream().toArray(Data[]::new);
        table.updateSettings(config);
    }
    private void setting() {
        config.stretchH = "all";
        config.rowHeaders = false;
        config.width = "100%";
        config.rowHeaderWidth = 30;
        selectAllHeader();
        selectRowHeader();
    }
    private void selectRowHeader() {
        config.afterGetRowHeaderRenderers = renderers -> {
            JsArray.asJsArray(renderers).push((row, th)->{
                var header = rowHeaders.computeIfAbsent(row, r->{
                    var checkbox = checkbox().style("vertical-align: middle;  --md-checkbox-outline-width: 1px;");
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

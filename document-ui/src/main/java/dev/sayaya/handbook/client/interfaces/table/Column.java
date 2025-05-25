package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.interfaces.table.function.CellEditor;
import dev.sayaya.handbook.client.interfaces.table.function.CellRenderer;
import dev.sayaya.handbook.client.interfaces.table.function.HeaderRenderer;
import jsinterop.annotations.*;
import lombok.Builder;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.jboss.elemento.Elements.label;

@JsType(isNative = true, namespace= JsPackage.GLOBAL, name="Object")
@Accessors(fluent=true)
@Setter(onMethod_={@JsOverlay, @JsIgnore})
public final class Column {
    @JsOverlay @JsIgnore
    public static Column defaults() {
        Column instance = new Column();
        instance.headerRenderer = n->label().add(instance.header!=null?instance.header:instance.data).element();
        return instance;
    }
    public String data;
    public String header;
    public String type;
    @JsProperty(name="_width")
    public Integer width;
    public String format;
    public String dateFormat;
    public Object source;
    public boolean strict;
    public boolean readOnly;
    public CellRenderer renderer;
    //private Validator validator;
    public HeaderRenderer headerRenderer;
    public boolean allowInvalid;
    public boolean allowEmpty;
    public CellEditorFn editor;
    public boolean filter;
    @JsFunction
    public interface CellEditorFn {
        CellEditor prototype(Object props);
    }
}

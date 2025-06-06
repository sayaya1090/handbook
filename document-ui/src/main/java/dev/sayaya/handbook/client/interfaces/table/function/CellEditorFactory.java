package dev.sayaya.handbook.client.interfaces.table.function;

import dev.sayaya.handbook.client.interfaces.table.Handsontable;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.HTMLTableCellElement;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public class CellEditorFactory {
    public native static CellEditorBase base(Object prop, CellEditorBaseImpl proxy)/*-{
		var CustomEditor = $wnd.Handsontable.editors.BaseEditor.prototype.extend();
		CustomEditor.prototype.prepare=function(r, c, p, t, v, e){
		    proxy.prepare(this.instance, r, c, p, t, v, e);
		}
		CustomEditor.prototype.setValue=function(e){
		    proxy.setValue(e);
		}
		CustomEditor.prototype.getValue=function() {
		    return proxy.getValue();
		}
		CustomEditor.prototype.open=function(evt) {
            proxy.open(this, evt);
		}
		CustomEditor.prototype.close=function() {
		    proxy.close();
		}
		CustomEditor.prototype.focus=function() {
		    proxy.focus();
		}
		return new CustomEditor(prop);
	}-*/;
    public native static CellEditorText text(Object prop, CellEditorTextImpl proxy)/*-{
		var CustomEditorText = $wnd.Handsontable.editors.TextEditor.prototype.extend();
		CustomEditorText.prototype.init = function() {
            $wnd.Handsontable.editors.TextEditor.prototype.init.apply(this, arguments);
            this.TEXTAREA = proxy.createElement();
            this.TEXTAREA.className += " handsontableInput"; // 기존 클래스에 추가
            this.textareaStyle = this.TEXTAREA.style;
            $wnd.Handsontable.dom.empty(this.TEXTAREA_PARENT);
            this.TEXTAREA_PARENT.appendChild(this.TEXTAREA);
            proxy.init(this);
        }
        CustomEditorText.prototype.setValue=function(value){
			proxy.prepare(this.instance, this.row, this.col, this.prop, this.TEXTAREA, value, this.cellProperties);
		    $wnd.Handsontable.editors.TextEditor.prototype.setValue.apply(this, arguments);
		    proxy.setValue(value);
		}
		CustomEditorText.prototype.getValue=function() {
		    return proxy.toValue($wnd.Handsontable.editors.TextEditor.prototype.getValue.apply(this, arguments));
		}
		CustomEditorText.prototype.beginEditing=function(value, evt) {
		    $wnd.Handsontable.editors.TextEditor.prototype.beginEditing.apply(this, arguments);
		    proxy.beginEditing(value, evt);
		}
		return new CustomEditorText(prop);
	}-*/;
    @JsType(isNative = true)
    public interface CellEditorBaseImpl {
        void prepare(Handsontable table, int row, int col, String prop, HTMLTableCellElement td, String value, Object cell);
        void setValue(String stringifiedInitialValue);
        String getValue();
        void open(Object e, Event event);
        void close();
        void focus();
    }
    @JsType(isNative = true)
    public interface CellEditorTextImpl {
        void init(CellEditorText editorInstance);
        Element createElement();
        void setValue(String stringfiedInitialValue);
        void prepare(Handsontable table, int row, int col, String prop, HTMLTableCellElement td, String value, Object cell);
        String toValue(String value);
        void beginEditing(String value, Event evt);
    }
    @JsType(isNative = true, namespace="Handsontable.editors", name="BaseEditor")
    public static abstract class CellEditorBase implements CellEditor {
        @JsProperty(name="instance") public Handsontable table;
        @JsProperty public int row;
        @JsProperty public int col;
        @JsProperty public String prop;
        @JsProperty(name="TD") public HTMLTableCellElement cell;
        CellEditorBase(Object prop){}
    }
    @JsType(isNative = true, namespace="Handsontable.editors", name="TextEditor")
    public static abstract class CellEditorText implements CellEditor {
        @JsProperty(name="instance") public Handsontable table;
        @JsProperty public int row;
        @JsProperty public int col;
        @JsProperty public String prop;
        @JsProperty(name="TD") public HTMLTableCellElement cell;
        CellEditorText(Object prop){}
    }
}

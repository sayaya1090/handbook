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
		// init: 에디터가 처음 생성될 때 딱 한 번 호출됩니다.
        CustomEditor.prototype.init = function(){
            $wnd.Handsontable.editors.BaseEditor.prototype.init.apply(this, arguments);
            // Java 프록시의 init을 호출하여, 에디터 자신(this)을 넘겨줍니다.
            // 이를 통해 Java는 나중에 finishEditing() 등을 호출할 수 있습니다.
            proxy.init(this);

            // 에디터의 UI를 담을 컨테이너 div를 만듭니다.
            var rootEl = this.instance.rootElement;
            this.editorDiv = rootEl.ownerDocument.createElement('div');
            this.editorDiv.className = 'handsontableInputHolder';
            this.editorDiv.style.display = 'none'; // 초기에는 숨김
            this.editorDiv.style.position = 'fixed';
            rootEl.appendChild(this.editorDiv);
        };
        CustomEditor.prototype.prepare=function(r, c, p, t, v, e){
            $wnd.Handsontable.editors.BaseEditor.prototype.prepare.apply(this, arguments);
		    proxy.prepare(this.instance, r, c, p, t, v, e);
		}
        // open: 편집이 시작될 때마다 호출됩니다.
        CustomEditor.prototype.open = function(evt) {
            // 이전에 추가된 자식이 있다면 제거합니다.
            while (this.editorDiv.firstChild) {
                this.editorDiv.removeChild(this.editorDiv.firstChild);
            }
            // Java 프록시로부터 에디터 UI 엘리먼트를 받아 컨테이너에 추가합니다.
            var element = proxy.getElement();
            this.editorDiv.appendChild(element);
            // Handsontable의 기본 스타일링을 적용하고, 셀 위에 위치시킵니다.
            var cellRect = this.TD.getBoundingClientRect();
            var docEl = this.TD.ownerDocument.documentElement; // documentElement를 가져옵니다.
            var scrollTop = $wnd.pageYOffset || docEl.scrollTop;
            var scrollLeft = $wnd.pageXOffset || docEl.scrollLeft;

            // documentElement의 테두리 두께를 가져옵니다. (없으면 0)
            var clientTop = docEl.clientTop || 0;
            var clientLeft = docEl.clientLeft || 0;

            // 최종 위치 = 뷰포트 위치 + 스크롤 오프셋 - 문서 테두리 두께
            this.editorDiv.style.top = (cellRect.top + scrollTop - clientTop) + 'px';
            this.editorDiv.style.left = (cellRect.left + scrollLeft - clientLeft) + 'px';
            this.editorDiv.style.width = cellRect.width + 'px';
            this.editorDiv.style.height = cellRect.height + 'px';
            this.editorDiv.style.display = 'block';

            // Java 프록시의 open 메서드를 호출하여 추가 로직을 실행합니다.
            proxy.open(this, evt);
        };
        // close: 편집이 종료될 때마다 호출됩니다.
        CustomEditor.prototype.close = function() {
            this.editorDiv.style.display = 'none'; // 컨테이너 숨김
            proxy.close();
        };
        // focus: 포커스를 맞춰야 할 때 호출됩니다.
        CustomEditor.prototype.focus = function() {
            proxy.focus(); // 실제 포커스 동작은 Java 프록시에 위임
        };
		CustomEditor.prototype.setValue=function(e){
		    proxy.setValue(e);
		}
		CustomEditor.prototype.getValue=function() {
		    return proxy.getValue();
		}
		return new CustomEditor(prop);
	}-*/;
    public native static CellEditorText text(Object prop, CellEditorTextImpl proxy)/*-{
		var CustomEditorText = $wnd.Handsontable.editors.TextEditor.prototype.extend();
		CustomEditorText.prototype.init = function() {
            $wnd.Handsontable.editors.TextEditor.prototype.init.apply(this, arguments);
            proxy.init(this);
        }
        CustomEditorText.prototype.createElements = function() {
			$wnd.Handsontable.editors.TextEditor.prototype.createElements.apply(this, arguments);
			this.TEXTAREA = proxy.createElement();
			this.TEXTAREA.className += " handsontableInput";
			this.textareaStyle = this.TEXTAREA.style;
			$wnd.Handsontable.dom.empty(this.TEXTAREA_PARENT);
			this.TEXTAREA_PARENT.appendChild(this.TEXTAREA);
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
        void init(CellEditorBase editorInstance);
        Element getElement();
        void setValue(String stringifiedInitialValue);
        void prepare(Handsontable table, int row, int col, String prop, HTMLTableCellElement td, String value, Object cell);
        String getValue();
        void open(CellEditorBase editorInstance, Event event);
        void close();
        void focus();
    }
    @JsType(isNative = true)
    public interface CellEditorTextImpl {
        void init(CellEditorText editorInstance);
        Element createElement(CellEditorText editorInstance);
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
        public native void finishEditing(boolean restoreOriginalValue, boolean ctrlDown, Runnable callback);
        public native void finishEditing();
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

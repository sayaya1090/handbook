package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.interfaces.table.event.HasStateChangeHandlers;
import dev.sayaya.handbook.client.interfaces.table.event.HasValueChangeHandlers;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import elemental2.core.ObjectPropertyDescriptor;
import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.gwtproject.event.shared.HandlerRegistration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@JsType
public class Data implements HasStateChangeHandlers<Data.DataState> {
    public static Data create(String id) {
        Data origin = new Data(id);
        ObjectPropertyDescriptor<Data> hide = Js.cast(new Object());
        hide.setEnumerable(false);
        for(var field: JsObject.getOwnPropertyNames(origin).asList()) JsObject.defineProperty(origin, field, hide);
        return proxy(origin, origin::fireValueChangeEvent);
    }
    @JsIgnore private final String id;
    @JsIgnore private final JsPropertyMap<Object> initializedValues;
    @JsIgnore private final JsPropertyMap<Object> validationValues;
    @JsIgnore private final List<StateChangeEventListener<DataState>> stateChangeListeners;
    @JsIgnore private final JsArray<HasValueChangeHandlers.ValueChangeEventListener<String>> valueChangeListeners;
    @JsIgnore private DataState state;
    private Data(String idx) {
        this.id = idx;
        initializedValues = JsPropertyMap.of();
        validationValues = JsPropertyMap.of();
        stateChangeListeners = new LinkedList<>();
        valueChangeListeners = JsArray.of();
    }
    @JsIgnore
    public Data put(String key, String value) {
        Js.asPropertyMap(this).set(key, value);
        if(!initializedValues.has(key)) initializedValues.set(key, value);
        return this;
    }
    @JsIgnore
    public Data delete(String key) {
        Js.asPropertyMap(this).delete(key);
        return this;
    }
    @JsIgnore
    public String get(String key) {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        if(!map.has(key)) return null;
        Object obj = map.get(key);
        if(obj instanceof String) return (String) obj;
        else return null;
    }
    @JsIgnore
    public boolean isChanged(String key) {
        var v1 = initializedValues.get(key);
        var v2 = get(key);
        if(v1 == null && v2 == null) return false;
        else if(v1 == null || v2 == null) return true;
        return !Js.isTripleEqual(trim(Js.asString(v1)), trim(Js.asString(v2)));
    }
    @JsIgnore
    public Data validity(String key, Boolean valid) {
        validationValues.set(key, valid);
        return this;
    }
    @JsIgnore
    public Boolean isValid(String key) {
        if(!validationValues.has(key)) return null;
        return (Boolean) validationValues.get(key);
    }
    private static String trim(String str) {
        if(str == null) return str;
        str = str.replace("\r", "").trim();
        if(str.isEmpty()) return null;
        else return str;
    }
    public List<String> keys() {
        return JsObject.getOwnPropertyNames(this).asList().stream().filter(key->!key.endsWith("$")).collect(Collectors.toUnmodifiableList());
    }
    @JsIgnore
    public boolean isChanged() {
        return JsObject.keys(initializedValues).asList().stream().anyMatch(this::isChanged);
    }
    @JsIgnore
    public Data select(boolean select) {
        state = select?DataState.SELECTED:DataState.UNSELECTED;
        fireStateChangeEvent();
        return this;
    }
    @JsIgnore @Override
    public Collection<StateChangeEventListener<DataState>> listeners() {
        return stateChangeListeners;
    }
    @JsIgnore @Override
    public DataState state() {
        return state;
    }
    @JsIgnore
    public HandlerRegistration onValueChange(HasValueChangeHandlers.ValueChangeEventListener<String> listener) {
        valueChangeListeners.push(listener);
        return () -> valueChangeListeners.delete(valueChangeListeners.asList().indexOf(listener));
    }
    @JsIgnore
    private void fireValueChangeEvent(String key) {
        var evt = HasValueChangeHandlers.ValueChangeEvent.event(new CustomEvent<>("change"), key);
        for (var listener : valueChangeListeners.asList()) {
            if (listener == null) break;
            listener.handle(evt);
        }
    }
    public enum DataState {
        UNSELECTED, SELECTED
    }
    private native static Data proxy(Data origin, ChangeHandler consumer) /*-{
		return new Proxy(origin, {
            set: function(target, key, value, receiver) {
                    if(target[key]==value) return true;
                    var result = Reflect.set(target, key, value, receiver);
                    if(result) consumer.@dev.sayaya.handbook.client.interfaces.table.Data.ChangeHandler::onInvoke(Ljava/lang/String;)(key);
                    return result;
                }
        });
	}-*/;
    private interface ChangeHandler {
        void onInvoke(String key);
    }
}

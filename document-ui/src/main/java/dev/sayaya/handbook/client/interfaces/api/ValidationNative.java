package dev.sayaya.handbook.client.interfaces.api;

import dev.sayaya.handbook.client.domain.Validation;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.HashMap;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public final class ValidationNative {
    @JsProperty public String status;
    @JsProperty public JsObject data;

    @JsOverlay @JsIgnore public Validation toDomain() {
        var builder = Validation.builder()
                .state(Validation.ValidationState.valueOf(status));
        if(data!=null) {
            JsPropertyMap<Boolean> map = Js.cast(data);
            map.forEach(key -> {
                var value = map.get(key);
                if(value != null) builder.value(key, value);
            });
        } else builder.values(new HashMap<>());
        DomGlobal.console.log(builder.build());
        return builder.build();
    }
}

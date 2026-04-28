package dev.sayaya.handbook.interfaces.api;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Position;
import elemental2.core.JsDate;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.LinkedHashMap;
import java.util.Map;

/** JSON ↔ Layout 변환. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class LayoutNative {
    public String id;
    @JsProperty(name = "effect_date_time") public String effectDateTime;
    @JsProperty(name = "expire_date_time") public String expireDateTime;
    public JsPropertyMap<PositionNative> positions;

    @JsOverlay
    public final LayoutPeriod toPeriod() {
        return LayoutPeriod.of(
                new JsDate(effectDateTime).getTime(),
                new JsDate(expireDateTime).getTime());
    }

    @JsOverlay
    public final Map<String, Position> toPositionMap() {
        Map<String, Position> map = new LinkedHashMap<>();
        if (positions != null) {
            positions.forEach(key -> {
                PositionNative p = positions.get(key);
                map.put(key, Position.of(p.x, p.y, p.width, p.height));
            });
        }
        return map;
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static class PositionNative {
        public int x;
        public int y;
        public int width;
        public int height;
    }
}

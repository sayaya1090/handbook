package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import jsinterop.base.JsPropertyMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class TypeLayout {
    @JsonProperty("id") @JsProperty(name = "id") private String id;
    @JsonProperty("workspace") @JsProperty(name = "workspace") private String workspace;
    @JsonProperty("effect_date_time") @JsProperty(name = "effect_date_time") private double effectDateTime;
    @JsonProperty("expire_date_time") @JsProperty(name = "expire_date_time") private double expireDateTime;
    @JsonProperty("positions") @JsProperty(name = "positions") private JsPropertyMap<Position> positions;
    @JsonProperty("rev") @JsProperty(name = "rev") private double rev;

    @JsOverlay @JsIgnore
    public static TypeLayout create(String id, String workspace, double effectDateTime, double expireDateTime, JsPropertyMap<Position> positions) {
        TypeLayout layout = new TypeLayout();
        layout.id(id);
        layout.workspace(workspace);
        layout.effectDateTime(effectDateTime);
        layout.expireDateTime(expireDateTime);
        layout.positions(positions);
        layout.rev(-1.0);
        return layout;
    }

    @JsOverlay @JsIgnore
    public LayoutPeriod toPeriod() {
        return LayoutPeriod.of(effectDateTime(), expireDateTime());
    }
}

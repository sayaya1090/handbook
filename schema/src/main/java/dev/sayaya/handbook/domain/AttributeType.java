package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
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
public final class AttributeType {
    @JsonProperty("type") @JsProperty(name = "type") public String type;
    @JsonProperty("referenced_type") @JsProperty(name = "referenced_type") public String referencedType;
    @JsonProperty("element_type") @JsProperty(name = "element_type") public AttributeType elementType;
    @JsonProperty("key_type") @JsProperty(name = "key_type") public AttributeType keyType;
    @JsonProperty("value_type") @JsProperty(name = "value_type") public AttributeType valueType;
    @JsonProperty("allowed_values") @JsProperty(name = "allowed_values") public String[] allowedValues;
    @JsonProperty("regex_patterns") @JsProperty(name = "regex_patterns") public String[] regexPatterns;
    @JsonProperty("min") @JsProperty(name = "min") public Double min;
    @JsonProperty("max") @JsProperty(name = "max") public Double max;
    @JsonProperty("after") @JsProperty(name = "after") public Double after;
    @JsonProperty("before") @JsProperty(name = "before") public Double before;
    @JsonProperty("extensions") @JsProperty(name = "extensions") public String[] extensions;

    @JsOverlay @JsIgnore
    public static AttributeType text() {
        AttributeType atv = new AttributeType();
        atv.type("text");
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType text(String[] regexPatterns) {
        AttributeType atv = text();
        atv.regexPatterns(regexPatterns);
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType enumType(String[] values) {
        AttributeType atv = new AttributeType();
        atv.type("enum");
        atv.allowedValues(values);
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType number() {
        AttributeType atv = new AttributeType();
        atv.type("number");
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType number(Double min, Double max) {
        AttributeType atv = number();
        atv.min(min);
        atv.max(max);
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType date() {
        AttributeType atv = new AttributeType();
        atv.type("date");
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType date(Double after, Double before) {
        AttributeType atv = date();
        atv.after(after);
        atv.before(before);
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType bool() {
        AttributeType atv = new AttributeType();
        atv.type("bool");
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType document(String referencedType) {
        AttributeType atv = new AttributeType();
        atv.type("document");
        atv.referencedType(referencedType);
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType array(AttributeType element) {
        AttributeType atv = new AttributeType();
        atv.type("array");
        atv.elementType(element);
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType map(AttributeType keyType, AttributeType valueType) {
        AttributeType atv = new AttributeType();
        atv.type("map");
        atv.keyType(keyType);
        atv.valueType(valueType);
        return atv;
    }

    @JsOverlay @JsIgnore
    public static AttributeType file(String[] extensions) {
        AttributeType atv = new AttributeType();
        atv.type("file");
        atv.extensions(extensions);
        return atv;
    }

    @JsOverlay
    public final String simplify() {
        AttributeType current = this;
        StringBuilder suffix = new StringBuilder();
        while (current != null && "array".equals(current.type) && current.elementType != null) {
            suffix.append("[]");
            current = current.elementType;
        }
        if (current == null) return "unknown" + suffix;

        String base;
        if ("document".equals(current.type)) {
            base = (current.referencedType != null) ? current.referencedType : "document";
        } else {
            base = (current.type != null) ? current.type : "unknown";
        }
        return base + suffix.toString();
    }
}

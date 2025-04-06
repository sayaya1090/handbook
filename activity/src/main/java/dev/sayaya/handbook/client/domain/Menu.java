package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.*;

import java.util.Objects;

@JsType(isNative=true, namespace= JsPackage.GLOBAL, name="Object")
public final class Menu {
    public String title;
    @JsProperty(name="supporting_text")
    public String supportingText;
    @JsProperty(name="icon_type")
    public String iconType;
    public String icon;
    @JsProperty(name="trailing_text")
    public String trailingText;
    public String order;
    public Page[] children;
    public Boolean bottom;
    @Override @JsOverlay @JsIgnore
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return Objects.equals(title, menu.title);
    }
    @Override @JsOverlay @JsIgnore
    public int hashCode() {
        return Objects.hash(title);
    }
}

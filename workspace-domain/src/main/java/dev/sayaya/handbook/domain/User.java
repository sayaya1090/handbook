package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
public final class User {
    private String id;
    private String name;
    private String email;

    @JsOverlay @JsIgnore
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String id;
        private String name;
        private String email;
        UserBuilder() {}
        public UserBuilder id(String id) {
            this.id = id;
            return this;
        }
        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        public User build() {
            var instance = new User();
            instance.id = id;
            instance.name = name;
            instance.email = email;
            return instance;
        }
    }
}

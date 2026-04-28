package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class User {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("email") @JsProperty public String email;

    @JsOverlay @JsIgnore
    public static User create(String id, String name, String email) {
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        return user;
    }
}

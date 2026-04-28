package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.*;
import lombok.experimental.Accessors;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class User {
    @JsonProperty("id") @JsProperty public String id;
    @JsonProperty("name") @JsProperty public String name;
    @JsonProperty("email") @JsProperty public String email;

    @JsOverlay @JsIgnore
    public static User create(String id, String name, String email) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID cannot be empty");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be empty");
        User user = new User();
        user.id = id;
        user.name = name;
        user.email = email;
        return user;
    }
}

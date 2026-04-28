package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

/**
 * 문서 타입 정보.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TypeInfo {
    @JsonProperty("id") public String id;
    @JsonProperty("version") public String version;
}

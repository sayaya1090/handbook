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
 * 문서 내 속성 정보.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttributeInfo {
    @JsonProperty("name") public String name;
    @JsonProperty("type") public String type;
    @JsonProperty("nullable") public boolean nullable;
    @JsonProperty("description") public String description;
}

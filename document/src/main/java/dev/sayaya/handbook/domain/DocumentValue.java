package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

/**
 * 백엔드(Kotlin)와 프론트엔드(GWT)가 공유하는 문서 도메인 모델.
 * 네이티브 JsType 규칙에 따라 필드는 public 이어야 한다.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentValue {
    @JsonProperty("id") public String id;
    @JsonProperty("type") public String type;
    @JsonProperty("serial") public String serial;
    @JsonProperty("effectDateTime") public double effectDateTime;
    @JsonProperty("expireDateTime") public double expireDateTime;
    @JsonProperty("createDateTime") public double createDateTime;
    @JsonProperty("creator") public String creator;
    @JsonProperty("data") public JsPropertyMap<String> data;
    @JsonProperty("status") @Builder.Default public String status = "DRAFT";
    @JsonProperty("rev") public double rev;

    @JsOverlay
    public final boolean isExpired(double now) {
        return expireDateTime > 0 && expireDateTime <= now;
    }
}

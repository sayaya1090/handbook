package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsinterop.annotations.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 타입 및 레이아웃의 원자적 일괄 변경을 위한 패치 객체.
 * 
 * <p><b>책임:</b> 생성/수정(UPSERT) 및 삭제(DELETE) 명령어를 리스트 형태로 담아 서버에 전송한다.
 * 서버는 이 리스트를 단일 트랜잭션 내에서 순차적으로 실행한다.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
@Getter(onMethod_ = {@JsOverlay, @JsIgnore})
@Setter(onMethod_ = {@JsOverlay, @JsIgnore})
@Accessors(fluent = true)
@NoArgsConstructor
public final class SchemaPatch {
    @JsonProperty("types") @JsProperty private TypeOperation[] types;
    @JsonProperty("layouts") @JsProperty private LayoutOperation[] layouts;

    @JsOverlay @JsIgnore
    public static SchemaPatch create(TypeOperation[] types, LayoutOperation[] layouts) {
        SchemaPatch patch = new SchemaPatch();
        patch.types(types);
        patch.layouts(layouts);
        return patch;
    }

    /** 타입 조작 명령 (UPSERT / DELETE) */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    @Getter(onMethod_ = {@JsOverlay, @JsIgnore})
    @Setter(onMethod_ = {@JsOverlay, @JsIgnore})
    @Accessors(fluent = true)
    @NoArgsConstructor
    public static final class TypeOperation {
        /** 작업 종류: "UPSERT" 또는 "DELETE" */
        @JsonProperty("op") @JsProperty private String op;
        /** 타입 데이터 */
        @JsonProperty("data") @JsProperty private Type data;

        @JsOverlay @JsIgnore
        public static TypeOperation upsert(Type data) {
            TypeOperation operation = new TypeOperation();
            operation.op("UPSERT");
            operation.data(data);
            return operation;
        }

        @JsOverlay @JsIgnore
        public static TypeOperation delete(Type data) {
            TypeOperation operation = new TypeOperation();
            operation.op("DELETE");
            operation.data(data);
            return operation;
        }
    }

    /** 레이아웃 조작 명령 (UPSERT / DELETE) */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    @Getter(onMethod_ = {@JsOverlay, @JsIgnore})
    @Setter(onMethod_ = {@JsOverlay, @JsIgnore})
    @Accessors(fluent = true)
    @NoArgsConstructor
    public static final class LayoutOperation {
        /** 작업 종류: "UPSERT" 또는 "DELETE" */
        @JsonProperty("op") @JsProperty private String op;
        /** 레이아웃 데이터 */
        @JsonProperty("data") @JsProperty private TypeLayout data;

        @JsOverlay @JsIgnore
        public static LayoutOperation upsert(TypeLayout data) {
            LayoutOperation operation = new LayoutOperation();
            operation.op("UPSERT");
            operation.data(data);
            return operation;
        }

        @JsOverlay @JsIgnore
        public static LayoutOperation delete(TypeLayout data) {
            LayoutOperation operation = new LayoutOperation();
            operation.op("DELETE");
            operation.data(data);
            return operation;
        }
    }
}

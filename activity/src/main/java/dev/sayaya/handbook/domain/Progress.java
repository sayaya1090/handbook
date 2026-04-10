package dev.sayaya.handbook.domain;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 프로그레스 바 상태를 나타내는 값 객체.
 * API 로딩(indeterminate)과 에이전트 진행률(value/max) 모두 지원한다.
 *
 * <p>사용 예:
 * <pre>
 * progress.next(Progress.indeterminate());          // API 호출 시작
 * progress.next(Progress.of(3, 10, "처리 중..."));  // 에이전트 진행률
 * progress.next(Progress.hide());                   // 완료 후 숨김
 * </pre>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Progress {
    private boolean enabled;
    private boolean intermediate;
    private double value;
    private double max;
    private String description;

    @JsOverlay @JsIgnore
    public boolean enabled() { return enabled; }
    @JsOverlay @JsIgnore
    public boolean intermediate() { return intermediate; }
    @JsOverlay @JsIgnore
    public double value() { return value; }
    @JsOverlay @JsIgnore
    public double max() { return max; }
    @JsOverlay @JsIgnore
    public String description() { return description; }

    /** API 로딩용 — 무한 프로그레스 */
    @JsOverlay @JsIgnore
    public static Progress indeterminate() {
        Progress p = new Progress();
        p.enabled = true;
        p.intermediate = true;
        p.value = 0;
        p.max = 1;
        p.description = null;
        return p;
    }

    /** 에이전트 진행률용 — value/max 기반 */
    @JsOverlay @JsIgnore
    public static Progress of(double value, double max, String description) {
        Progress p = new Progress();
        p.enabled = true;
        p.intermediate = false;
        p.value = value;
        p.max = max;
        p.description = description;
        return p;
    }

    /** 프로그레스 바 숨김 */
    @JsOverlay @JsIgnore
    public static Progress hide() {
        Progress p = new Progress();
        p.enabled = false;
        p.intermediate = false;
        p.value = 0;
        p.max = 1;
        p.description = null;
        return p;
    }
}

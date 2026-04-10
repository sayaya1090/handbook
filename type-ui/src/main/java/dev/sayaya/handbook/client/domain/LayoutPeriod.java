package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 레이아웃 기간을 나타내는 값 객체(GWT JsInterop native).
 *
 * <p><b>책임:</b> effectDateTime ~ expireDateTime 범위를 표현하며,
 * 두 기간 간 겹침(overlap) 계산 기능을 제공한다.
 * 타입 목록 조회 시 기간 파라미터로 사용된다.</p>
 * <p><b>의존관계:</b> 없음 (자립 값 객체)</p>
 * <p><b>주의:</b> 시각값은 epoch 밀리초(double)로 저장된다. JsDate 변환 시 정밀도에 주의.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class LayoutPeriod {
    public double effectDateTime;
    public double expireDateTime;

    @JsOverlay
    public static LayoutPeriod of(double effectDateTime, double expireDateTime) {
        LayoutPeriod p = new LayoutPeriod();
        p.effectDateTime = effectDateTime;
        p.expireDateTime = expireDateTime;
        return p;
    }

    @JsOverlay
    public final double overlap(LayoutPeriod other) {
        double start = Math.max(effectDateTime, other.effectDateTime);
        double end = Math.min(expireDateTime, other.expireDateTime);
        return Math.max(0, end - start);
    }
}

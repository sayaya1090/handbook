package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * 문서 검색 쿼리 및 페이지네이션 상태를 표현하는 값 객체.
 *
 * <p><b>책임:</b> 현재 선택된 타입, 페이지 번호, 페이지 크기, 검색 필터 목록을 보유한다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class Search {
    public String type;
    public int page;
    public int limit;
    @JsProperty(name = "total_pages")
    public int totalPages;
    @JsProperty(name = "total_elements")
    public double totalElements;

    @JsOverlay
    public static Search defaultSearch() {
        Search s = new Search();
        s.page = 0;
        s.limit = 20;
        return s;
    }
}

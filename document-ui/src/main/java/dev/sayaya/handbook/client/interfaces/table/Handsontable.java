package dev.sayaya.handbook.client.interfaces.table;

import elemental2.dom.Element;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Handsontable JS 라이브러리의 JsInterop 래퍼.
 *
 * <p><b>책임:</b> 네이티브 Handsontable 6.2.4 MIT 인스턴스를 GWT에서 사용할 수 있도록
 * 생성, 렌더링, 설정 변경, 셀 선택/편집, 행 추가/삭제, 파괴 등의 API를 노출한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link HandsontableConfig} — 테이블 초기화 및 설정 갱신에 사용되는 설정 객체</li>
 *   <li>{@link Column} — 컬럼별 타입, 너비, 읽기전용 등의 설정</li>
 * </ul></p>
 *
 * <p><b>주의:</b> native JsType이므로 Java 측에서 메서드를 오버라이드하거나 확장할 수 없다.
 * Handsontable JS 라이브러리가 페이지에 로드되어 있어야 한다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Handsontable")
public final class Handsontable {
    @JsConstructor
    public Handsontable(Element element, HandsontableConfig setting) {}

    @JsProperty
    public Element container;

    public native void render();
    public native void updateSettings(HandsontableConfig setting);
    public native HandsontableConfig getSettings();
    public native int countRows();
    public native int countCols();
    public native boolean selectCell(int row, int column);
    public native void deselectCell();
    public native Element getCell(int row, int col, boolean topmost);
    public native void setDataAtCell(int row, int col, Object value);
    public native void alter(String action, int index, int amount);
    public native void destroy();
}

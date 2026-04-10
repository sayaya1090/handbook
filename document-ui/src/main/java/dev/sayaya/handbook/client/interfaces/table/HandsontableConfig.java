package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Handsontable 설정 객체.
 *
 * <p><b>책임:</b> Handsontable 초기화 및 updateSettings 호출 시 전달되는 설정값을 담는다.
 * 데이터 배열, 컬럼 정의, 헤더, 크기, 고정 컬럼 수, 라이선스 키 등을 포함한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link Column} — columns 필드에 사용되는 컬럼별 설정 배열</li>
 * </ul></p>
 *
 * <p><b>주의:</b> native JS Object로 매핑되므로 Java에서 메서드를 추가할 수 없다.
 * 필드에 직접 값을 할당하여 사용한다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class HandsontableConfig {
    public Object[][] data;
    public String stretchH;
    public Object width;
    public Object height;
    public Integer minRows;
    public boolean manualColumnResize;
    public boolean autoRowSize;
    public boolean autoColSize;
    public Column[] columns;
    public Object colHeaders;
    public boolean readOnly;
    public Integer fixedColumnsLeft;
    public String licenseKey;
}

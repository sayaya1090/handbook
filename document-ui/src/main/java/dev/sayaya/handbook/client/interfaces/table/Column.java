package dev.sayaya.handbook.client.interfaces.table;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Handsontable 개별 컬럼 설정.
 *
 * <p><b>책임:</b> 단일 컬럼의 데이터 바인딩 키, 셀 타입, 너비, 읽기전용 여부를 정의한다.
 * {@link HandsontableConfig#columns} 배열의 요소로 사용된다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ColumnFactory} — {@link dev.sayaya.handbook.client.domain.ColumnDef}로부터 이 객체를 생성</li>
 * </ul></p>
 *
 * <p><b>주의:</b> native JS Object로 매핑되므로 생성자나 메서드를 추가할 수 없다.
 * 필드에 직접 값을 할당하여 사용한다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class Column {
    public String data;
    public String type;
    public Integer width;
    public Boolean readOnly;
    /** Handsontable dropdown/autocomplete source options (enum 등). */
    public String[] source;
    /** Handsontable date format string (예: "YYYY-MM-DD HH:mm"). */
    public String dateFormat;
    /** true이면 Handsontable이 잘못된 날짜 형식을 자동 보정한다. */
    public Boolean correctFormat;
}

package dev.sayaya.handbook.client.domain;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * 속성 정보. 타입의 속성을 Handsontable 컬럼으로 변환할 때 사용한다.
 *
 * <p><b>책임:</b> 백엔드에서 전달된 속성 메타데이터를 GWT에서 사용 가능한 JS 객체로 매핑한다.
 * 타입별 추가 정보(enum의 허용값, document의 참조 타입, array의 원소 타입)를 포함하여
 * {@link ColumnDef} 생성 시 Handsontable에 타입별 에디터/렌더러를 적용할 수 있게 한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 데이터 객체)</p>
 *
 * <p><b>주의:</b> native JS Object로 매핑되므로 메서드를 추가할 수 없다.
 * 타입별 필드는 해당 타입이 아닐 경우 null/undefined이다.</p>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class AttributeInfo {
    public String name;
    public String type;
    public boolean nullable;
    public String description;
    /** enum 타입의 허용 값 목록. enum이 아니면 null. */
    public String[] allowedValues;
    /** document 타입이 참조하는 타입 이름. document가 아니면 null. */
    public String referencedType;
    /** array 타입의 원소 타입 이름 (예: "text", "number"). array가 아니면 null. */
    public String elementType;
}

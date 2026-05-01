package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.ColumnDef;
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Type의 속성을 Handsontable 컬럼 정의로 변환하는 팩토리.
 *
 * <p><b>책임:</b> {@link Type}의 속성 목록을 읽어 serial, effectDateTime, expireDateTime
 * 고정 컬럼과 동적 속성 컬럼을 포함하는 {@link dev.sayaya.handbook.client.domain.ColumnDef} 배열을 생성한다.
 * document 타입 속성은 현재 레이아웃의 타입 이름 목록을 드롭다운으로 제공한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link Type} — 컬럼 생성의 원본 타입 정보</li>
 *   <li>{@link dev.sayaya.handbook.domain.Attribute} — 동적 컬럼의 속성 정보</li>
 *   <li>{@link dev.sayaya.handbook.client.domain.ColumnDef} — 생성되는 컬럼 정의 도메인 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 유틸리티 클래스이므로 인스턴스를 생성할 수 없다(private 생성자).
 * 고정 컬럼 3개(serial, effectDateTime, expireDateTime)는 항상 포함된다.</p>
 */
public final class ColumnFactory {
    private ColumnFactory() {}

    public static ColumnDef[] create(Type type) {
        return create(type, null);
    }

    /**
     * 타입 정보와 현재 레이아웃의 전체 타입 목록을 기반으로 컬럼 배열을 생성한다.
     * @param type 현재 선택된 타입
     * @param allTypes 현재 레이아웃의 전체 타입 목록 (document 속성의 드롭다운에 사용)
     */
    public static ColumnDef[] create(Type type, List<Type> allTypes) {
        List<ColumnDef> defs = new ArrayList<>();
        defs.add(ColumnDef.serial());
        defs.add(ColumnDef.effectDateTime());
        defs.add(ColumnDef.expireDateTime());
        String[] typeNames = allTypes != null
                ? allTypes.stream().map(Type::id).toArray(String[]::new)
                : null;
        if (type != null && type.attributes() != null) {
            for (Attribute attr : type.attributes()) {
                defs.add(ColumnDef.fromAttribute(attr, typeNames));
            }
        }
        return defs.toArray(new ColumnDef[0]);
    }
}

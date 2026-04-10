package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.AttributeInfo;
import dev.sayaya.handbook.client.domain.ColumnDef;
import dev.sayaya.handbook.client.domain.TypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * TypeInfo의 속성을 Handsontable 컬럼 정의로 변환하는 팩토리.
 *
 * <p><b>책임:</b> {@link TypeInfo}의 속성 목록을 읽어 serial, effectDateTime, expireDateTime
 * 고정 컬럼과 동적 속성 컬럼을 포함하는 {@link dev.sayaya.handbook.client.domain.ColumnDef} 배열을 생성한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link TypeInfo} — 컬럼 생성의 원본 타입 정보</li>
 *   <li>{@link dev.sayaya.handbook.client.domain.AttributeInfo} — 동적 컬럼의 속성 정보</li>
 *   <li>{@link dev.sayaya.handbook.client.domain.ColumnDef} — 생성되는 컬럼 정의 도메인 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 유틸리티 클래스이므로 인스턴스를 생성할 수 없다(private 생성자).
 * 고정 컬럼 3개(serial, effectDateTime, expireDateTime)는 항상 포함된다.</p>
 */
public final class ColumnFactory {
    private ColumnFactory() {}

    public static ColumnDef[] create(TypeInfo type) {
        List<ColumnDef> defs = new ArrayList<>();
        // 고정 컬럼: serial, effectDateTime, expireDateTime
        defs.add(ColumnDef.serial());
        defs.add(ColumnDef.effectDateTime());
        defs.add(ColumnDef.expireDateTime());
        // 동적 컬럼: 타입의 속성들
        if (type != null && type.attributes != null) {
            for (AttributeInfo attr : type.attributes) {
                defs.add(ColumnDef.fromAttribute(attr));
            }
        }
        return defs.toArray(new ColumnDef[0]);
    }
}

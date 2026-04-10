package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.AttributeInfo;
import dev.sayaya.handbook.client.domain.ColumnDef;
import dev.sayaya.handbook.client.domain.TypeInfo;

import java.util.ArrayList;
import java.util.List;

/** TypeInfo의 속성을 Handsontable 컬럼 정의로 변환한다. */
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

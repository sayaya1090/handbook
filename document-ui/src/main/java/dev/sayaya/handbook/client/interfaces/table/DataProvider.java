package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.ColumnDef;
import dev.sayaya.handbook.domain.DocumentValue;
import jsinterop.base.JsPropertyMap;

import java.util.ArrayList;
import java.util.List;

/**
 * DocumentValue 도메인 객체와 Handsontable 2D 배열 간 양방향 변환을 담당한다.
 *
 * <p><b>책임:</b> {@link dev.sayaya.handbook.domain.DocumentValue} 목록을
 * Handsontable이 요구하는 {@code Object[][]} 형식으로 변환(toData)하고,
 * 반대로 2D 배열을 DocumentValue 목록으로 역변환(fromData)한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link dev.sayaya.handbook.client.domain.ColumnDef} — 컬럼 이름을 기준으로 필드 매핑</li>
 *   <li>{@link dev.sayaya.handbook.domain.DocumentValue} — 변환 대상 도메인 객체</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 유틸리티 클래스이므로 인스턴스를 생성할 수 없다.
 * serial, effectDateTime, expireDateTime은 고정 필드로 특별 처리되며,
 * 그 외 필드는 DocumentValue.data(JsPropertyMap)에서 동적으로 읽고 쓴다.</p>
 */
public final class DataProvider {
    private DataProvider() {}

    /** 문서 목록을 Handsontable 데이터 배열로 변환한다. */
    public static Object[][] toData(List<DocumentValue> docs, ColumnDef[] columns) {
        if (docs == null || docs.isEmpty() || columns == null) return new Object[0][0];
        Object[][] result = new Object[docs.size()][columns.length];
        for (int r = 0; r < docs.size(); r++) {
            DocumentValue doc = docs.get(r);
            for (int c = 0; c < columns.length; c++) {
                String name = columns[c].name();
                result[r][c] = switch (name) {
                    case "serial" -> doc.serial();
                    case "effectDateTime" -> doc.effectDateTime();
                    case "expireDateTime" -> doc.expireDateTime();
                    default -> doc.data() != null ? doc.data().get(name) : null;
                };
            }
        }
        return result;
    }

    /** Handsontable 2D 배열을 DocumentValue 목록으로 변환한다. */
    public static List<DocumentValue> fromData(Object[][] data, ColumnDef[] columns, List<DocumentValue> originals) {
        List<DocumentValue> docs = new ArrayList<>();
        if (data == null || columns == null) return docs;
        for (int r = 0; r < data.length; r++) {
            DocumentValue doc = (r < originals.size()) ? originals.get(r) : new DocumentValue();
            for (int c = 0; c < columns.length; c++) {
                String name = columns[c].name();
                String value = data[r][c] != null ? String.valueOf(data[r][c]) : null;
                switch (name) {
                    case "serial" -> doc.serial(value);
                    default -> {
                        if (doc.data() == null) doc.data(JsPropertyMap.of());
                        doc.data().set(name, value);
                    }
                }
            }
            docs.add(doc);
        }
        return docs;
    }
}

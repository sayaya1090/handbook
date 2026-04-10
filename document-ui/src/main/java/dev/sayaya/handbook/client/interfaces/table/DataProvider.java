package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.ColumnDef;
import dev.sayaya.handbook.client.domain.DocumentValue;
import jsinterop.base.JsPropertyMap;

import java.util.ArrayList;
import java.util.List;

/** DocumentValue 도메인 객체와 Handsontable 2D 배열 간 변환을 담당한다. */
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
                    case "serial" -> doc.serial;
                    case "effectDateTime" -> doc.effectDateTime;
                    case "expireDateTime" -> doc.expireDateTime;
                    default -> doc.data != null ? doc.data.get(name) : null;
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
                    case "serial" -> doc.serial = value;
                    default -> {
                        if (doc.data == null) doc.data = JsPropertyMap.of();
                        doc.data.set(name, value);
                    }
                }
            }
            docs.add(doc);
        }
        return docs;
    }
}

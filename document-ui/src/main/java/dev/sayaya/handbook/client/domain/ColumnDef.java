package dev.sayaya.handbook.client.domain;

import dev.sayaya.handbook.domain.Attribute;

/**
 * Handsontable 컬럼 정의. 속성 타입에 따라 type/width/readOnly/source/dateFormat 등을 결정한다.
 *
 * <p><b>책임:</b> 도메인 속성 정보({@link Attribute})를 Handsontable이 이해할 수 있는
 * 컬럼 설정으로 변환한다.</p>
 */
public record ColumnDef(
    String name,
    String label,
    String type,
    int width,
    boolean readOnly,
    String[] source,
    String dateFormat,
    Boolean correctFormat
) {
    public static ColumnDef of(String name, String label, String type, int width, boolean readOnly) {
        return new ColumnDef(name, label, type, width, readOnly, null, null, null);
    }

    public static ColumnDef serial() {
        return of("serial", "Serial", "text", 150, false);
    }

    public static ColumnDef effectDateTime() {
        return new ColumnDef("effectDateTime", "Effective", "date", 120, false,
                null, "YYYY-MM-DD HH:mm", true);
    }

    public static ColumnDef expireDateTime() {
        return new ColumnDef("expireDateTime", "Expire", "date", 120, false,
                null, "YYYY-MM-DD HH:mm", true);
    }

    public static ColumnDef status() {
        return of("status", "Status", "text", 100, true);
    }

    public static ColumnDef fromAttribute(Attribute attr) {
        return fromAttribute(attr, null);
    }

    public static ColumnDef fromAttribute(Attribute attr, String[] typeNames) {
        String attrType = (attr.type() != null && attr.type().type() != null) ? attr.type().type() : "text";
        switch (attrType) {
            case "number":
                return of(attr.name(), attr.name(), "numeric", 120, false);
            case "date":
                return new ColumnDef(attr.name(), attr.name(), "date", 120, false,
                        null, "YYYY-MM-DD", true);
            case "bool":
                return of(attr.name(), attr.name(), "checkbox", 120, false);
            case "enum":
                return new ColumnDef(attr.name(), attr.name(), "dropdown", 120, false,
                        attr.type().allowedValues(), null, null);
            case "document":
                return new ColumnDef(attr.name(), attr.name(), "dropdown", 150, false,
                        null, null, null);
            default:
                return of(attr.name(), attr.name(), "text", 120, false);
        }
    }
}

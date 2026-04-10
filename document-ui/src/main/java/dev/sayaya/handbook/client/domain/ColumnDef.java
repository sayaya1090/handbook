package dev.sayaya.handbook.client.domain;

/** Handsontable 컬럼 정의. 속성 타입에 따라 type/width/readOnly 등을 결정한다. */
public record ColumnDef(
    String name,
    String label,
    String type,
    int width,
    boolean readOnly
) {
    public static ColumnDef serial() {
        return new ColumnDef("serial", "Serial", "text", 150, false);
    }

    public static ColumnDef effectDateTime() {
        return new ColumnDef("effectDateTime", "Effective", "date", 120, false);
    }

    public static ColumnDef expireDateTime() {
        return new ColumnDef("expireDateTime", "Expire", "date", 120, false);
    }

    public static ColumnDef fromAttribute(AttributeInfo attr) {
        String hotType = switch (attr.type) {
            case "number" -> "numeric";
            case "date" -> "date";
            case "bool" -> "checkbox";
            case "enum" -> "dropdown";
            default -> "text";
        };
        return new ColumnDef(attr.name, attr.name, hotType, 120, false);
    }
}

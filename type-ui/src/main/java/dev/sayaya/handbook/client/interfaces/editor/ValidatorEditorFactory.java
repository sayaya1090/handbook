package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.usecase.TypeList;

/**
 * 타입 이름으로 ValidatorEditor를 동적 생성하는 팩토리.
 *
 * <p><b>책임:</b> Array/Map의 서브 타입 에디터를 재귀적으로 생성한다.
 * 무한 재귀를 방지하기 위해 최대 깊이를 제한한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link TypeList} — document 타입 에디터에 전달</li>
 * </ul></p>
 */
public class ValidatorEditorFactory {
    private static final int MAX_DEPTH = 3;
    private final TypeList typeList;
    private final int depth;

    public ValidatorEditorFactory(TypeList typeList) {
        this(typeList, 0);
    }

    private ValidatorEditorFactory(TypeList typeList, int depth) {
        this.typeList = typeList;
        this.depth = depth;
    }

    /**
     * 타입 이름에 맞는 ValidatorEditor를 생성한다.
     * bool 등 추가 설정이 없는 타입은 null을 반환한다.
     */
    public ValidatorEditor create(String type) {
        if (type == null) return null;
        return switch (type) {
            case "text" -> new TextValidatorEditor();
            case "number" -> new NumberValidatorEditor();
            case "date" -> new DateValidatorEditor();
            case "enum" -> new EnumValidatorEditor();
            case "file" -> new FileValidatorEditor();
            case "document" -> new DocumentValidatorEditor(typeList);
            case "array" -> depth < MAX_DEPTH ? new ArrayValidatorEditor(nested()) : null;
            case "map" -> depth < MAX_DEPTH ? new MapValidatorEditor(nested()) : null;
            default -> null;
        };
    }

    /** 한 단계 깊은 팩토리를 반환한다. */
    private ValidatorEditorFactory nested() {
        return new ValidatorEditorFactory(typeList, depth + 1);
    }

    /** 현재 깊이가 최대에 도달했는지 여부. */
    public boolean isMaxDepth() {
        return depth >= MAX_DEPTH;
    }
}

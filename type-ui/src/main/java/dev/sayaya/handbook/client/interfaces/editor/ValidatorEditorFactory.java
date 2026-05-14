package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.client.usecase.TypeList;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 플러그인 형태로 등록된 공급자들을 사용하여 ValidatorEditor를 동적 생성하는 팩토리.
 *
 * <p><b>책임:</b> 등록된 에디터 공급자들로부터 에디터 인스턴스를 생성한다.
 * Array/Map의 서브 타입 에디터를 재귀적으로 생성하며, 무한 재귀 방지를 위해 깊이를 제한한다.</p>
 */
public class ValidatorEditorFactory {
    private static final int MAX_DEPTH = 3;
    private final Map<String, ValidatorEditorProvider> providers;
    private final TypeList typeList;
    private final int depth;

    @Inject
    public ValidatorEditorFactory(Map<String, ValidatorEditorProvider> providers, TypeList typeList) {
        this(providers, typeList, 0);
    }

    private ValidatorEditorFactory(Map<String, ValidatorEditorProvider> providers, TypeList typeList, int depth) {
        this.providers = providers;
        this.typeList = typeList;
        this.depth = depth;
    }

    /** 모든 지원 타입에 대한 에디터를 생성합니다. */
    public Map<String, ValidatorEditor> createAll() {
        Map<String, ValidatorEditor> editors = new HashMap<>();
        for (String type : providers.keySet()) {
            ValidatorEditor editor = create(type);
            if (editor != null) editors.put(type, editor);
        }
        return editors;
    }

    /**
     * 타입 이름에 맞는 ValidatorEditor를 생성한다.
     * 지원하지 않거나 에디터가 없는 타입은 null을 반환한다.
     */
    public ValidatorEditor create(String type) {
        if (type == null || !providers.containsKey(type)) return null;
        return providers.get(type).create(this);
    }

    /** 중첩 에디터 생성을 위해 한 단계 깊은 팩토리 인스턴스를 반환한다. */
    public ValidatorEditorFactory nested() {
        return new ValidatorEditorFactory(providers, typeList, depth + 1);
    }

    /** 현재 깊이가 최대에 도달했는지 여부. */
    public boolean isMaxDepth() {
        return depth >= MAX_DEPTH;
    }

    public TypeList getTypeList() {
        return typeList;
    }
}

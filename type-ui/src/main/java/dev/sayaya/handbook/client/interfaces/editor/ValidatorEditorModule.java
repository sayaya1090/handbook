package dev.sayaya.handbook.client.interfaces.editor;

import dagger.Module;
import dagger.Provides;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ValidatorEditor 공급자들을 수동으로 등록하여 제공하는 모듈.
 * Dagger Multibindings의 Guava 의존성 문제를 피하면서도 OCP를 준수하기 위해
 * 중앙 집중식 맵 생성을 사용합니다.
 */
@Module
public class ValidatorEditorModule {
    @Provides
    static Map<String, ValidatorEditorProvider> provideProviders() {
        Map<String, ValidatorEditorProvider> map = new HashMap<>();
        
        map.put("text", factory -> new TextValidatorEditor());
        map.put("number", factory -> new NumberValidatorEditor());
        map.put("date", factory -> new DateValidatorEditor());
        map.put("enum", factory -> new EnumValidatorEditor());
        map.put("file", factory -> new FileValidatorEditor());
        map.put("document", factory -> new DocumentValidatorEditor(factory.getTypeList()));
        map.put("array", factory -> factory.isMaxDepth() ? null : new ArrayValidatorEditor(factory.nested()));
        map.put("map", factory -> factory.isMaxDepth() ? null : new MapValidatorEditor(factory.nested()));
        
        return map;
    }
}

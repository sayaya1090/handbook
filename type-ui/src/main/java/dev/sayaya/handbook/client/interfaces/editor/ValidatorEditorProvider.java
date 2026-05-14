package dev.sayaya.handbook.client.interfaces.editor;

/**
 * 특정 타입에 대한 ValidatorEditor 인스턴스를 생성하는 공급자 인터페이스.
 */
@FunctionalInterface
public interface ValidatorEditorProvider {
    /**
     * @param factory 현재 팩토리 인스턴스 (중첩 에디터나 공용 리소스 접근 시 필요)
     * @return 생성된 ValidatorEditor 인스턴스. 해당 타입에 에디터가 필요 없는 경우 null을 반환할 수 있습니다.
     */
    ValidatorEditor create(ValidatorEditorFactory factory);
}

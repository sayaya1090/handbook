package dev.sayaya.handbook.client.interfaces.editor;

import dev.sayaya.handbook.domain.AttributeType;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

/** 타입별 validator 에디터 인터페이스. */
public interface ValidatorEditor extends IsElement<HTMLElement> {
    /** 현재 AttributeType의 값을 에디터 UI에 반영한다. */
    void load(AttributeType value);
    /** 에디터 UI의 입력값을 AttributeType에 반영하여 반환한다. */
    AttributeType collect();
}

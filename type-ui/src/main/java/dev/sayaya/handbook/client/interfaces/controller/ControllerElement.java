package dev.sayaya.handbook.client.interfaces.controller;

import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 캔버스 좌측 도구 모음(Toolbox) 컨테이너.
 *
 * <p><b>책임:</b> 개체 생성(Add) 및 편집 모드 전환(ModeToggle), 삭제 도구 등 
 * 캔버스의 직접적인 조작을 담당하는 도구들을 배치한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link ModeToggleButton} — LAYOUT/TYPE 모드 전환</li>
 *   <li>{@link AddTypeButton} — 신규 타입 생성</li>
 *   <li>{@link RemoveTypeButton} — 단일 타입 삭제</li>
 *   <li>{@link BulkDeleteButton} — 일괄 타입 삭제</li>
 * </ul></p>
 * <p><b>주의:</b> 글로벌 액션(Undo, Save 등)은 {@link StatusHeaderElement} 로 이관되었다.
 * CSS 클래스 "type-controller"로 스타일링된다.</p>
 */
@Singleton
public class ControllerElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> _this = div();

    @Inject
    ControllerElement(AddTypeButton addBtn, NewVersionButton newVersionBtn,
                      RemoveTypeButton removeBtn, BulkDeleteButton bulkDeleteBtn) {
        _this.css("type-controller")
                .add(div().css("type-ctrl-group")
                        .add(addBtn).add(newVersionBtn).add(removeBtn).add(bulkDeleteBtn));
    }
}

package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeProvider;
import dev.sayaya.handbook.domain.Type;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 타입 탭 선택 컴포넌트.
 *
 * <p><b>책임:</b> {@link TypeList}를 구독하여 사용 가능한 타입 목록을 탭으로 렌더링하고,
 * 탭 클릭 시 {@link TypeProvider#next}를 호출하여 현재 선택 타입을 변경한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link TypeList} — 전체 타입 목록 상태 구독</li>
 *   <li>{@link TypeProvider} — 선택된 타입 상태 갱신</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 타입 목록이 변경되면 기존 탭을 모두 제거하고 다시 렌더링한다(innerHTML 초기화).</p>
 */
@Singleton
public class TypeTabsElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;
    private final TypeProvider typeProvider;

    @Inject
    public TypeTabsElement(TypeList typeList, TypeProvider typeProvider) {
        this.typeProvider = typeProvider;
        this.element = div().css("doc-type-tabs").element();
        typeList.subscribe(this::renderTabs);
    }

    private void renderTabs(List<Type> types) {
        element.innerHTML = "";
        if (types == null) return;
        for (Type type : types) {
            var tab = span().css("doc-type-tab")
                    .text(type.id())
                    .on(EventType.click, e -> typeProvider.next(type))
                    .element();
            element.appendChild(tab);
        }
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}

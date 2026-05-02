package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.domain.Type;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/**
 * 컨텍스트 메뉴 공용 유틸리티 클래스.
 *
 * <p><b>책임:</b> 컨텍스트 메뉴 항목 DOM 생성(menuItem)과
 * 중복되지 않는 유니크 타입 ID 생성(uniqueTypeId)을 제공한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeList} — uniqueTypeId에서 기존 타입 ID 중복 검사</li>
 * </ul></p>
 * <p><b>주의:</b> 유틸 클래스이므로 인스턴스 생성 불가(private 생성자).
 * uniqueTypeId는 "new-type-N" 형식으로 순차 생성한다.</p>
 */
public final class ContextMenuHelper {
    private ContextMenuHelper() {}

    public static HTMLElement menuItem(String text) {
        HTMLDivElement item = div().css("ctx-item").element();
        item.textContent = text;
        return item;
    }

    /** TypeList에서 중복되지 않는 유니크 타입 ID를 생성한다. */
    public static String uniqueTypeId(TypeList typeList) {
        int n = 1;
        while (true) {
            String id = "new-type-" + n;
            boolean exists = false;
            for (Type t : typeList.getValue()) {
                if (t.id.equals(id)) { exists = true; break; }
            }
            if (!exists) return id;
            n++;
        }
    }
}

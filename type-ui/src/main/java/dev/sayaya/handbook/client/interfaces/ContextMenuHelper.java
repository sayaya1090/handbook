package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.TypeList;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

/** 컨텍스트 메뉴 공용 유틸. */
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
            for (TypeValue t : typeList.getValue()) {
                if (t.id.equals(id)) { exists = true; break; }
            }
            if (!exists) return id;
            n++;
        }
    }
}

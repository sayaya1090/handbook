package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.Element;
import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import static dev.sayaya.ui.elements.NavigationElementBuilder.railItem;

/**
 * MenuRail / ToolRail 의 항목. sayaya-ui {@code railItem()}({@code md-navigation-rail-item}) 를 감싼다.
 *
 * <p><b>책임:</b> 아이콘(outline/filled 두 벌, {@code inactive-icon} slot), 라벨, 선택 상태를 렌더한다.
 * 선택 상태는 host 의 {@code [selected]}(기존 셀렉터 호환) + {@code active} property + 부모 rail 의
 * {@code activeIndex} 로 동기화한다 — rail 이 updated 때마다 activeIndex 기준으로 active 를 다시 계산하므로.</p>
 * <p><b>주의점:</b> 접힌 rail 에서는 라벨을 비워 아이콘만 보이게 한다(md-navigation-rail-item 은 접힌 상태에서도
 * 라벨을 아이콘 아래에 그리기 때문). 라벨 표시 여부는 rail 컨테이너가 {@link #showLabel(boolean)} 로 정한다.
 * 펼친 상태의 보조 텍스트·하위 도구 표시(▶)는 라이브러리에 슬롯이 없어 표시하지 않는다.</p>
 */
public abstract class RailItemElement implements IsElement<HTMLElement> {
    private final HTMLElement _this = railItem().css("item", "nav-rail-item").element();
    private String labelText = "";
    private boolean labelVisible = false;

    /** 아이콘을 추가한다. outline/filled 두 벌을 모두 넣고 표시 전환은 shell.css(.icon-outline/.icon-filled) 가 한다. */
    public RailItemElement icon(Element icon) {
        icon.setAttribute("slot", "inactive-icon");
        _this.appendChild(icon);
        return this;
    }
    public RailItemElement icon(IconElementBuilder icon) { return icon(icon.element()); }

    public RailItemElement label(String text) {
        this.labelText = text != null ? text : "";
        applyLabel();
        return this;
    }
    public String label() { return labelText; }

    /** rail 이 펼쳐졌을 때만 라벨을 보인다. */
    public void showLabel(boolean visible) {
        this.labelVisible = visible;
        applyLabel();
    }
    private void applyLabel() {
        Js.asPropertyMap(_this).set("label", labelVisible ? labelText : "");
        if (labelText.isEmpty()) _this.removeAttribute("aria-label");
        else _this.setAttribute("aria-label", labelText);
    }

    public void select(boolean value) {
        if (value) _this.setAttribute("selected", true);
        else _this.removeAttribute("selected");
        Js.asPropertyMap(_this).set("active", value);
        Element parent = _this.parentElement;
        if (parent == null || !"md-navigation-rail".equalsIgnoreCase(parent.tagName)) return;
        int index = indexIn(parent);
        double current = Js.asPropertyMap(parent).has("activeIndex") ? Js.asPropertyMap(parent).getAsAny("activeIndex").asDouble() : -1;
        if (value) Js.asPropertyMap(parent).set("activeIndex", index);
        else if (current == index) Js.asPropertyMap(parent).set("activeIndex", -1);
    }
    private int indexIn(Element rail) {
        int i = 0;
        for (Element cur = rail.firstElementChild; cur != null; cur = cur.nextElementSibling) {
            if (!"md-navigation-rail-item".equalsIgnoreCase(cur.tagName)) continue;
            if (cur == _this) return i;
            i++;
        }
        return -1;
    }

    public <V extends Event> void on(EventType<V, ?> type, EventCallbackFn<V> callback) {
        _this.addEventListener(type.name, evt -> callback.onEvent(Js.cast(evt)));
    }

    @Override
    public HTMLElement element() { return _this; }
}

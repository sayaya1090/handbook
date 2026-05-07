package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import java.util.ArrayList;
import java.util.List;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.htmlElement;

/**
 * 모바일용 확장형 플로팅 버튼 (Speed Dial).
 * 
 * <p><b>책임:</b> 메인 FAB을 노출하고, 클릭 시 서브 메뉴 버튼들을 애니메이션과 함께 펼친다.</p>
 */
public class SpeedDialElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final HTMLElement mainFab;
    private final HTMLDivElement menu;
    private final List<HTMLElement> items = new ArrayList<>();
    private boolean expanded = false;

    public SpeedDialElement(String icon, String colorClass) {
        this.menu = div().css("speed-dial-menu").element();
        var fab = htmlElement("md-fab", HTMLElement.class)
                .attr("variant", "primary")
                .css(colorClass)
                .on(EventType.click, e -> toggle());
        fab.element().appendChild((elemental2.dom.Element) IconElementBuilder.icon().attr("slot", "icon").css("fa-sharp", "fa-solid", icon).element());
        this.mainFab = fab.element();
        
        this.root = div().css("type-speed-dial")
                .add(menu)
                .add(mainFab)
                .element();
        
        // 외부 터치 시 닫기
        elemental2.dom.DomGlobal.document.addEventListener("click", evt -> {
            if (expanded && !root.contains((elemental2.dom.Node) evt.target)) {
                collapse();
            }
        });
    }

    public SpeedDialElement addItem(IsElement<? extends HTMLElement> item) {
        HTMLElement el = item.element();
        if (!items.contains(el)) {
            items.add(el);
            menu.appendChild(el);
        }
        return this;
    }

    public void clearItems() {
        items.clear();
        while (menu.firstChild != null) menu.removeChild(menu.firstChild);
    }

    public void toggle() {
        if (expanded) collapse();
        else expand();
    }

    public void expand() {
        expanded = true;
        root.setAttribute("expanded", "");
        mainFab.setAttribute("variant", "secondary");
    }

    public void collapse() {
        expanded = false;
        root.removeAttribute("expanded");
        mainFab.setAttribute("variant", "primary");
    }

    public SpeedDialElement css(String... classes) {
        for (String c : classes) root.classList.add(c);
        return this;
    }

    @Override
    public HTMLDivElement element() { return root; }
}

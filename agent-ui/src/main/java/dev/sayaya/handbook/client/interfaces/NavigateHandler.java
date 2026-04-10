package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.NavigateInfo;
import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.rx.Observer;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * navigate 커맨드를 처리한다.
 * Shell의 URI Observer에 URL을 발행하여 HistoryManager/UrlBasedMenuResolver가 화면을 전환한다.
 * 이동 내용을 인디케이터로 표시한다.
 */
@Singleton
public class NavigateHandler implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    NavigateHandler(AgentCommandDispatcher dispatcher, Observer<String> uri) {
        root = div().css("agent-navigate-indicator").element();
        root.style.set("display", "none");

        dispatcher.navigations().subscribe(nav -> {
            if (nav == null) return;
            if (nav.url() != null) uri.next(nav.url());
            showIndicator(nav);
        });
    }

    private void showIndicator(NavigateInfo nav) {
        StringBuilder text = new StringBuilder("\u27A4 ");
        if (nav.menu() != null) text.append(nav.menu());
        if (nav.tool() != null) text.append(" > ").append(nav.tool());
        if (nav.url() != null) text.append("  (").append(nav.url()).append(")");

        root.textContent = text.toString();
        root.style.set("display", "flex");

        DomGlobal.setTimeout(e -> {
            root.classList.add("agent-navigate-fadeout");
            DomGlobal.setTimeout(e2 -> {
                root.style.set("display", "none");
                root.classList.remove("agent-navigate-fadeout");
            }, 500);
        }, 2000);
    }

    @Override
    public HTMLDivElement element() { return root; }
}

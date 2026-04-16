package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.domain.NavigateCommand;
import dev.sayaya.rx.Observer;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * navigate 커맨드를 처리하여 화면을 전환하고 이동 인디케이터를 표시하는 핸들러.
 *
 * <p><b>책임:</b> Shell의 URI Observer에 URL을 발행하여 화면을 전환하고, 이동 내용을 2초간 인디케이터로 표시 후 페이드아웃한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — navigate 스트림 구독</li>
 *   <li>{@link Observer}&lt;String&gt; — Shell URI Observer (HistoryManager/UrlBasedMenuResolver 연동)</li>
 * </ul></p>
 */
@Singleton
public class NavigateHandler implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    NavigateHandler(AgentCommandDispatcher dispatcher, Observer<String> uri) {
        root = div().css("agent-navigate-indicator").element();
        root.style.set("display", "none");

        dispatcher.navigations().subscribe(cmd -> {
            if (cmd == null) return;
            if (cmd.url() != null) uri.next(cmd.url());
            showIndicator(cmd);
        });
    }

    private void showIndicator(NavigateCommand cmd) {
        StringBuilder text = new StringBuilder("\u27A4 ");
        if (cmd.menu() != null) text.append(cmd.menu());
        if (cmd.tool() != null) text.append(" > ").append(cmd.tool());
        if (cmd.url() != null) text.append("  (").append(cmd.url()).append(")");

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

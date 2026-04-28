package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.usecase.AgentMutation;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * mutate 커맨드를 처리하여 변경 로그를 표시하고 편집 모듈에 전파하는 핸들러.
 *
 * <p><b>책임:</b> AgentMutation를 통해 변경사항을 편집 모듈에 CustomEvent로 전달하고, 변경 로그를 화면에 3초간 표시 후 페이드아웃한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link AgentCommandDispatcher} — mutation 스트림 구독</li>
 *   <li>{@link AgentMutation} — CustomEvent 기반 모듈 간 통신</li>
 * </ul></p>
 */
@Singleton
public class MutateHandler implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    MutateHandler(AgentCommandDispatcher dispatcher) {
        root = div().css("agent-mutate-log").element();
        root.style.set("display", "none");

        dispatcher.mutations().subscribe(cmd -> {
            if (cmd == null) return;
            String[] changes = cmd.changes();

            // 편집 모듈로 mutation 전달 (CustomEvent 기반)
            AgentMutation.publish(changes);

            // 변경 로그 화면 표시
            root.innerHTML = "";
            root.style.set("display", "block");

            for (String change : changes) {
                HTMLDivElement line = div().css("agent-mutate-line").element();
                line.textContent = "\u2022 " + change;
                root.appendChild(line);
            }

            DomGlobal.setTimeout(e -> {
                root.classList.add("agent-mutate-fadeout");
                DomGlobal.setTimeout(e2 -> {
                    root.style.set("display", "none");
                    root.classList.remove("agent-mutate-fadeout");
                    root.innerHTML = "";
                }, 500);
            }, 3000);
        });
    }

    @Override
    public HTMLDivElement element() { return root; }
}

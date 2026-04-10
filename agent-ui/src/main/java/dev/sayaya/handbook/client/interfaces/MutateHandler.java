package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.usecase.AgentCommandDispatcher;
import dev.sayaya.handbook.usecase.WindowMutationBridge;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * mutate 커맨드를 처리한다.
 * 변경 내역을 화면에 표시하고, WindowMutationBridge를 통해 편집 모듈(type-ui/workspace-ui)에 전달한다.
 */
@Singleton
public class MutateHandler implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    MutateHandler(AgentCommandDispatcher dispatcher) {
        root = div().css("agent-mutate-log").element();
        root.style.set("display", "none");

        dispatcher.mutations().subscribe(changes -> {
            if (changes == null) return;

            // 편집 모듈로 mutation 전달 (CustomEvent 기반)
            WindowMutationBridge.publish(changes);

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

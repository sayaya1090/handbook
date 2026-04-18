package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.handbook.client.interfaces.WorkspaceStylesheet;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 워크스페이스 생성 화면 전체 컨텐츠. 다이얼로그를 중앙에 배치한다.
 *
 * <p>엔트리 포인트 DI 그래프의 최상위 노드이므로 {@link WorkspaceStylesheet} 를
 * 생성자로 받아 workspace-ui 의 CSS(`css/workspace.css`) 가 반드시 주입되도록 한다.</p>
 */
@Singleton
public class ContentElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    ContentElement(DialogElement dialog, WorkspaceStylesheet stylesheet) {
        root = div().css("ws-content")
                .add(dialog)
                .element();
    }

    @Override
    public HTMLDivElement element() { return root; }
}

package dev.sayaya.handbook.client.interfaces.create;

import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/** 워크스페이스 생성 화면 전체 컨텐츠. 다이얼로그를 중앙에 배치한다. */
@Singleton
public class ContentElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    ContentElement(DialogElement dialog) {
        root = div().css("ws-content")
                .add(dialog)
                .element();
    }

    @Override
    public HTMLDivElement element() { return root; }
}

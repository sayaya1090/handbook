package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.BoxDisplayMode;
import dev.sayaya.ui.elements.CardElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import org.jboss.elemento.HTMLContainerBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.sayaya.ui.elements.CardElementBuilder.card;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

/*
  mode에 따라 다르게 출력한다.
    SIMPLE: 이름만 출력
    DETAIL: 이름, 설명, 속성 출력
 */
public class BoxElement extends HTMLContainerBuilder<HTMLDivElement> {
    private static final Map<Box, BoxElement> cache = new ConcurrentHashMap<>(); // 생성된 Box 재사용
    public static BoxElement of(Box box, dev.sayaya.handbook.client.usecase.BoxDisplayMode mode) {
        return cache.computeIfAbsent(box, b -> new BoxElement(box, mode));
    }

    private BoxElement(Box box, dev.sayaya.handbook.client.usecase.BoxDisplayMode mode) {
        this(div(), box, mode);
    }
    private final Box box;
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private final CardElementBuilder<?, ?> card;
    private final HTMLContainerBuilder<HTMLLabelElement> title = label().css("label");
    private BoxElement(HTMLContainerBuilder<HTMLDivElement> container, Box box, dev.sayaya.handbook.client.usecase.BoxDisplayMode mode) {
        super(container.element());
        if (box == null) throw new IllegalArgumentException("Box must not be null.");
        this.box = box;
        this.container = container;
        this.card = card().outlined().css("card");
        container.css("type-box")
                 .add(card.add(title));
        paint();
        mode.subscribe(this::changeMode);
    }
    private void changeMode(BoxDisplayMode mode) {
        if(mode == BoxDisplayMode.SIMPLE) {
            container.attr("simple", true);
            container.element().removeAttribute("detail");
        } else if(mode == BoxDisplayMode.DETAIL) {
            container.attr("detail", true);
            container.element().removeAttribute("simple");
        }
    }
    public Box toDomain() {
        return box;
    }
    public void paint() {
        title.text(box.name());
        container.element().style.left = box.x() + "px";
        container.element().style.top = box.y() + "px";
        card.element().style.width = CSSProperties.WidthUnionType.of(box.width() + "px");
        card.element().style.height = CSSProperties.HeightUnionType.of(box.height() + "px");
    }
}

package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.BoxDisplayMode;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;

/*
  mode에 따라 다르게 출력한다.
    SIMPLE: 이름만 출력
    DETAIL: 이름, 설명, 속성 출력
 */
public class BoxElement extends HTMLContainerBuilder<HTMLDivElement> {
    public BoxElement(Box box, dev.sayaya.handbook.client.usecase.BoxDisplayMode mode) {
        this(div(), box, mode);
    }
    private final Box box;
    private final HTMLContainerBuilder<HTMLDivElement> container;
    private BoxElement(HTMLContainerBuilder<HTMLDivElement> container, Box box, dev.sayaya.handbook.client.usecase.BoxDisplayMode mode) {
        super(container.element());
        this.box = box;
        this.container = container;
        mode.subscribe(this::changeMode);
        container.css("type-box").style("position: absolute; border: 1px solid black; padding: 1em; width: 100px; height: 80px;");
        container.add(box.name());
        container.element().style.left = (box.x()-50) + "px";
        container.element().style.top = (box.y()-40) + "px";
    }
    private void changeMode(BoxDisplayMode mode) {
        if(mode == BoxDisplayMode.SIMPLE) container.attr("simple", true).attr("detail", false);
        else if(mode == BoxDisplayMode.DETAIL) container.attr("detail", true).attr("simple", false);
    }
    public Box toDomain() {
        return box;
    }
}

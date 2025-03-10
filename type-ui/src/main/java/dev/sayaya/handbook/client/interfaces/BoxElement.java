package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.BoxDisplayMode;
import elemental2.core.JsObject;
import elemental2.core.ObjectPropertyDescriptor;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import org.jboss.elemento.HTMLContainerBuilder;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

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
    private final HTMLContainerBuilder<HTMLLabelElement> title = label();
    private BoxElement(HTMLContainerBuilder<HTMLDivElement> container, Box box, dev.sayaya.handbook.client.usecase.BoxDisplayMode mode) {
        super(container.element());
        if (box == null) throw new IllegalArgumentException("Box must not be null.");
        this.box = box;
        this.container = container;
        mode.subscribe(this::changeMode);
        container.css("type-box").style("""
            position: absolute;
            border: 1px solid rgba(0, 0, 0, 0.4);
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
            margin: 1em;
            background-color: #f8f9fa;
            transition: left 300ms cubic-bezier(0.25, 1.25, 0.5, 1.1),
                        top 300ms cubic-bezier(0.25, 1.25, 0.5, 1.1),
                        width 300ms cubic-bezier(0.25, 1.25, 0.5, 1.1),
                        height 300ms cubic-bezier(0.25, 1.25, 0.5, 1.1),
                        background-color 300ms ease,
                        box-shadow 300ms ease;
            """);
        container.add(title);
        paint();
    }
    private void changeMode(BoxDisplayMode mode) {
        if(mode == BoxDisplayMode.SIMPLE) container.attr("simple", true).attr("detail", false);
        else if(mode == BoxDisplayMode.DETAIL) container.attr("detail", true).attr("simple", false);
    }
    public Box toDomain() {
        return box;
    }
    public void paint() {
        title.text(box.name());
        container.element().style.left = (box.x()) + "px";
        container.element().style.top = (box.y()) + "px";
        container.element().style.width = CSSProperties.WidthUnionType.of(box.width() + "px");
        container.element().style.height = CSSProperties.HeightUnionType.of(box.height() + "px");
    }
}

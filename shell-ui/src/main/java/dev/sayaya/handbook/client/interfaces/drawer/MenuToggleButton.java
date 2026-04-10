package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.usecase.DrawerMode;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.DrawerState.COLLAPSE;
import static dev.sayaya.handbook.client.domain.DrawerState.EXPAND;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;

@Singleton
public class MenuToggleButton implements IsElement<HTMLElement> {
    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private final Element lineTop = createPath("M 80,29 H 20 C 20,29 5.501161,28.817352 5.467013,66.711331 5.456858,77.980673 9.033919,81.670246 14.740827,81.668997 20.447739,81.667751 25,75 25,75 L 75,25");
    private final Element lineMiddle = createPath("M 80,50 H 20");
    private final Element lineBottom = createPath("M 80,71 H 20 C 20,71 5.501161,71.182648 5.467013,33.288669 5.456858,22.019327 9.033919,18.329754 14.740827,18.331003 20.447739,18.332249 25,25 25,25 L 75,75");
    @Delegate private final IconButtonElementBuilder.PlainIconButtonElementBuilder icon = new IconButtonElementBuilder.PlainIconButtonElementBuilder().add(createSvg());
    private final DrawerMode mode;
    @Inject MenuToggleButton(DrawerMode mode) {
        this.mode = mode;
        icon.element().id = "menu-toggle-button";
        styleLine(lineTop);
        styleLine(lineMiddle);
        lineMiddle.setAttribute("style", lineMiddle.getAttribute("style") + "stroke-dasharray: " + MIDDLE_STROKE_ARRAY_COLLAPSED + ";");
        styleLine(lineBottom);
        initEventHandlers();
    }
    private Element createSvg() {
        var svg = DomGlobal.document.createElementNS(SVG_NS, "svg");
        svg.setAttribute("viewBox", "0 0 100 100");
        svg.appendChild(lineTop);
        svg.appendChild(lineMiddle);
        svg.appendChild(lineBottom);
        return svg;
    }
    private static Element createPath(String d) {
        var path = DomGlobal.document.createElementNS(SVG_NS, "path");
        path.setAttribute("d", d);
        return path;
    }
    private static void styleLine(Element line) {
        line.setAttribute("style", BASE_LINE_STYLE);
    }
    private void initEventHandlers() {
        mode.subscribe(this::handleDrawerStateChange);
        on(EventType.click, evt -> toggleDrawerState());
    }
    private void handleDrawerStateChange(DrawerState state) {
        if (state == EXPAND || state == DrawerState.OVERLAY) open();
        else if (state == COLLAPSE || state == DrawerState.HIDE) close();
    }
    private void toggleDrawerState() {
        if (mode.isMobile()) {
            mode.toggleOverlay();
        } else {
            DrawerState nextState = (mode.getValue() == EXPAND) ? COLLAPSE : EXPAND;
            mode.next(nextState);
        }
    }

    private void open() {
        icon.element().style.transform = "scaleX(-1)";
        setStroke(lineTop, STROKE_ARRAY_EXPANDED, STROKE_OFFSET_EXPANDED);
        setStroke(lineMiddle, MIDDLE_STROKE_ARRAY_EXPANDED, MIDDLE_STROKE_OFFSET_EXPANDED);
        setStroke(lineBottom, STROKE_ARRAY_EXPANDED, STROKE_OFFSET_EXPANDED);
    }
    private void close() {
        icon.element().style.transform = null;
        setStroke(lineTop, STROKE_ARRAY_COLLAPSED, null);
        setStroke(lineMiddle, MIDDLE_STROKE_ARRAY_COLLAPSED, null);
        setStroke(lineBottom, STROKE_ARRAY_COLLAPSED, null);
    }
    private static void setStroke(Element line, String dasharray, String dashoffset) {
        var style = BASE_LINE_STYLE + "stroke-dasharray: " + dasharray + ";";
        if(dashoffset != null) style += "stroke-dashoffset: " + dashoffset + ";";
        line.setAttribute("style", style);
    }

    private static final String STROKE_ARRAY_EXPANDED = "90 207";
    private static final String STROKE_ARRAY_COLLAPSED = "60 207";
    private static final String STROKE_OFFSET_EXPANDED = "-134";
    private static final String MIDDLE_STROKE_ARRAY_EXPANDED = "1 60";
    private static final String MIDDLE_STROKE_ARRAY_COLLAPSED = "60 60";
    private static final String MIDDLE_STROKE_OFFSET_EXPANDED = "-30";
    private static final String BASE_LINE_STYLE =
            "fill: none;" +
                    "stroke: var(--_icon-color);" +
                    "transition: stroke-dasharray 500ms cubic-bezier(.4,0,.2,1),stroke-dashoffset 500ms cubic-bezier(.4,0,.2,1);" +
                    "stroke-width: 5;" +
                    "transform: scale(1.3);" +
                    "transform-origin: 50%;";
}

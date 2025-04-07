package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.DrawerState;
import dev.sayaya.handbook.client.usecase.DrawerMode;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.svg.elements.SvgPathBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.handbook.client.domain.DrawerState.COLLAPSE;
import static dev.sayaya.handbook.client.domain.DrawerState.EXPAND;
import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.svg.elements.SvgBuilder.svg;
import static dev.sayaya.ui.svg.elements.SvgPathBuilder.path;

@Singleton
public class MenuToggleButton implements IsElement<HTMLElement> {
    private final SvgPathBuilder lineTop = path().d("M 80,29 H 20 C 20,29 5.501161,28.817352 5.467013,66.711331 5.456858,77.980673 9.033919,81.670246 14.740827,81.668997 20.447739,81.667751 25,75 25,75 L 75,25");
    private final SvgPathBuilder lineMiddle = path().d("M 80,50 H 20");
    private final SvgPathBuilder lineBottom = path().d("M 80,71 H 20 C 20,71 5.501161,71.182648 5.467013,33.288669 5.456858,22.019327 9.033919,18.329754 14.740827,18.331003 20.447739,18.332249 25,25 25,25 L 75,75");
    @Delegate private final IconButtonElementBuilder.PlainIconButtonElementBuilder icon = button().icon().add(svg().viewBox(0, 0, 100, 100).add(lineTop).add(lineMiddle).add(lineBottom).element());
    private final DrawerMode mode;
    @Inject MenuToggleButton(DrawerMode mode) {
        this.mode = mode;
        icon.element().id = "menu-toggle-button";
        styleLine(lineTop);
        styleLine(lineMiddle).style("stroke-dasharray", MIDDLE_STROKE_ARRAY_COLLAPSED);
        styleLine(lineBottom);
        initEventHandlers();
    }
    private void initEventHandlers() {
        mode.subscribe(this::handleDrawerStateChange);
        on(EventType.click, evt -> toggleDrawerState());
    }
    private void handleDrawerStateChange(DrawerState state) {
        if (state == EXPAND) open();
        else if (state == COLLAPSE) close();
    }
    private void toggleDrawerState() {
        DrawerState nextState = (mode.getValue() == EXPAND) ? COLLAPSE : EXPAND;
        mode.next(nextState);
    }

    private static SvgPathBuilder styleLine(SvgPathBuilder line) {
        return line.style(BASE_LINE_STYLE);
    }

    private void open() {
        icon.element().style.transform = "scaleX(-1)";
        lineTop.element().style.strokeDasharray = STROKE_ARRAY_EXPANDED;
        lineTop.element().style.strokeDashoffset = STROKE_OFFSET_EXPANDED;
        lineMiddle.element().style.strokeDasharray = MIDDLE_STROKE_ARRAY_EXPANDED;
        lineMiddle.element().style.strokeDashoffset = MIDDLE_STROKE_OFFSET_EXPANDED;
        lineBottom.element().style.strokeDasharray = STROKE_ARRAY_EXPANDED;
        lineBottom.element().style.strokeDashoffset = STROKE_OFFSET_EXPANDED;

    }
    private void close() {
        icon.element().style.transform = null;
        lineTop.element().style.strokeDasharray = STROKE_ARRAY_COLLAPSED;
        lineTop.element().style.strokeDashoffset = null;
        lineMiddle.element().style.strokeDasharray = MIDDLE_STROKE_ARRAY_COLLAPSED;
        lineMiddle.element().style.strokeDashoffset = null;
        lineBottom.element().style.strokeDasharray = STROKE_ARRAY_COLLAPSED;
        lineBottom.element().style.strokeDashoffset = null;
    }

    // 애니메이션 상태 값
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
                    "stroke-dasharray: 60 207;" +
                    "stroke-width: 5;" +
                    "transform: scale(1.3);" +
                    "transform-origin: 50%;";

}

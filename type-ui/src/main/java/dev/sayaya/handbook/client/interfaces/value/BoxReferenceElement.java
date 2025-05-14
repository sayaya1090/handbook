package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.interfaces.box.BoxElement;
import dev.sayaya.handbook.client.usecase.arrow.ArrowFactory;
import dev.sayaya.handbook.client.usecase.arrow.Rectangle;
import dev.sayaya.rx.Subscription;
import dev.sayaya.ui.svg.dom.SVGElement;
import dev.sayaya.ui.svg.elements.*;
import elemental2.dom.DOMRectReadOnly;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import static dev.sayaya.rx.Observable.timer;
import static org.jboss.elemento.Elements.body;

class BoxReferenceElement implements IsSvgElement<SVGElement, SvgBuilder> {
    private final String ARROW_HEAD_ID = "arrowhead" + Double.toString(Math.random()).substring(2, 7);
    private final SvgGroupBuilder group = SvgGroupBuilder.g();
    private final SvgGroupBuilder lines = SvgGroupBuilder.g();
    @Delegate private final SvgBuilder svg = SvgBuilder.svg().css("arrow").style("overflow: visible; pointer-events: none;")
            .add(group.style("pointer-events: auto;")
                    .add(SvgMarkerBuilder.marker().id(ARROW_HEAD_ID)
                            .viewBox(0, 0, 10, 10)
                            .refX(7).refY(5)
                            .markerWidth(6).markerHeight(5)
                            .orient("auto-start-reverse")
                            .add(SvgPathBuilder.path().d("M 0 0 L 10 5 L 0 10 z"))
                    ).add(lines));
    private final ValueElement start;
    private final BoxElement target;
    @AssistedInject BoxReferenceElement(@Assisted ValueElement start, @Assisted BoxElement target) {
        this.start = start;
        this.target = target;
        paint();
    }
    private static Rectangle toRect(IsElement<?> element, DOMRectReadOnly canvasRect) {
        var elRect = element.element().getBoundingClientRect();
        return new Rectangle(
                (int) (elRect.x - canvasRect.x),
                (int) (elRect.y - canvasRect.y),
                (int) elRect.width,
                (int) elRect.height
        );
    }
    public void paint() {
        clearLines(lines.element());
        var canvasRect = body().element().getElementsByClassName("canvas").item(0).getBoundingClientRect();
        var startRect = toRect(start, canvasRect);
        var targetRect = toRect(target, canvasRect);
        var arrow = ArrowFactory.createArrow(startRect, targetRect);
        var points = arrow.pathPoints();
        if (points == null || points.isEmpty()) {
            DomGlobal.console.warn("No points generated for arrow.");
            svg.attr("width", "0").attr("height", "0"); // SVG를 보이지 않게 처리
            return;
        }
        // 4. 경로의 바운딩 박스 계산 (canvas 기준 좌표)
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (var p : points) {
            minX = Math.min(minX, p.x());
            minY = Math.min(minY, p.y());
            maxX = Math.max(maxX, p.x());
            maxY = Math.max(maxY, p.y());
        }

        // 5. SVG 요소의 절대 위치 및 크기 설정
        // SVG의 left, top은 canvasRect를 기준으로 한 minX, minY에 canvasRect의 페이지 좌표를 더한 값입니다.
        double svgLeft = canvasRect.x + minX;
        double svgTop = canvasRect.y + minY;
        double svgWidth = Math.max(1, (maxX - minX));
        double svgHeight = Math.max(1, (maxY - minY));

        svg.attr("width", String.valueOf(svgWidth))
                .attr("height", String.valueOf(svgHeight))
                .style("position: fixed; left: " + svgLeft + "px; top: " + svgTop + "px; pointer-events: none;");

        // 6. 경로를 SVG 내부 좌표로 변환하여 Path 데이터 생성
        StringBuilder pathData = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            var p = points.get(i);
            // SVG 내부 좌표로 변환: (canvas 상대 좌표 - minCanvasRelativeCoord + PADDING)
            double svgPathX = p.x() - minX;
            double svgPathY = p.y() - minY;

            if (i == 0) pathData.append("M ").append(svgPathX).append(" ").append(svgPathY);
            else pathData.append(" L ").append(svgPathX).append(" ").append(svgPathY);
        }

        if (pathData.length() > 0) {
            // 보이는 화살표 라인
            var visiblePath = SvgPathBuilder.path().d(pathData.toString()).fill("none").attr("marker-end", "url(#" + ARROW_HEAD_ID + ")");
            lines.add(visiblePath);
            // 투명한 상호작용 라인 (더 두껍게)
            var interactionPath = SvgPathBuilder.path().d(pathData.toString()).fill("none").stroke("transparent").strokeWidth(10); // 상호작용을 위한 두께
            lines.add(interactionPath);
        }
    }
    private native void clearLines(Element elem) /*-{
        elem.innerHTML = "";
    }-*/;
    public void clear() {
        svg.clear();
    }

    @AssistedFactory
    interface BoxReferenceElementFactory {
        BoxReferenceElement director(ValueElement start, BoxElement target);
    }
}

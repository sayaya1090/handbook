package dev.sayaya.handbook.client.interfaces.value;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.interfaces.box.TypeElement;
import dev.sayaya.handbook.client.usecase.arrow.ArrowFactory;
import dev.sayaya.handbook.client.usecase.arrow.Point;
import dev.sayaya.handbook.client.usecase.arrow.Rectangle;
import dev.sayaya.ui.svg.dom.SVGElement;
import dev.sayaya.ui.svg.elements.*;
import elemental2.dom.DOMRectReadOnly;
import elemental2.dom.Element;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.elemento.Elements.body;

class BoxReferenceElement implements IsSvgElement<SVGElement, SvgBuilder> {
    private final String ARROW_HEAD_ID = "arrowhead" + Double.toString(Math.random()).substring(2, 7);
    private static final double CORNER_RADIUS = 10.0; // 모서리 호의 반지름 R
    private static final double EPSILON = 0.001; // 부동소수점 비교를 위한 작은 값

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
    private final TypeElement target;
    @AssistedInject BoxReferenceElement(@Assisted ValueElement start, @Assisted TypeElement target) {
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
        double svgLeft = minX;
        double svgTop = minY;
        double svgWidth = Math.max(1, (maxX - minX));
        double svgHeight = Math.max(1, (maxY - minY));

        svg.attr("width", String.valueOf(svgWidth))
                .attr("height", String.valueOf(svgHeight))
                .style("position: absolute; left: " + svgLeft + "px; top: " + svgTop + "px; pointer-events: none;");

        // 6. 경로를 SVG 내부 좌표로 변환하여 Path 데이터 생성
        List<Point> svgPoints = new ArrayList<>();
        for (Point p : points) {
            // SVG 내부 좌표로 변환: (canvas 상대 좌표 - minCanvasRelativeCoord + PADDING)
            svgPoints.add(new Point(p.x() - minX, p.y() - minY));
        }

        if (svgPoints.isEmpty()) { // originalPoints가 비어있지 않으면 여기 도달 안함
            return;
        }

        StringBuilder pathData = new StringBuilder();
        pathData.append("M ").append(svgPoints.get(0).x()).append(" ").append(svgPoints.get(0).y());
        double currentX = svgPoints.get(0).x();
        double currentY = svgPoints.get(0).y();

        for (int i = 1; i < svgPoints.size(); ) { // 현재 처리 대상 점의 인덱스 (p1)
            Point p1 = svgPoints.get(i); // 현재 점 (코너 후보 또는 직선의 끝점)

            // 1. 호(arc) 그리기 시도 (p1이 코너일 경우)
            // 호를 그리려면 p1 다음 점(p2)이 필요함
            if (i + 1 < svgPoints.size() && CORNER_RADIUS > EPSILON) {
                Point p2 = svgPoints.get(i + 1); // 코너 다음 점

                // (currentX, currentY) --- p1 --- p2 에서 p1이 코너
                // 세 점이 일직선 상에 있는지 확인 (일직선이면 호를 그리지 않음)
                // (y1-y0)*(x2-x1) == (y2-y1)*(x1-x0)
                boolean isCollinear = Math.abs((p1.y() - currentY) * (p2.x() - p1.x()) -
                        (p2.y() - p1.y()) * (p1.x() - currentX)) < EPSILON;

                if (!isCollinear) {
                    double p0x_arc = currentX; // 이전 점
                    double p0y_arc = currentY;
                    double p1x_arc = p1.x();   // 코너 점
                    double p1y_arc = p1.y();
                    double p2x_arc = p2.x();   // 다음 점
                    double p2y_arc = p2.y();

                    double v1x = p0x_arc - p1x_arc; // p1 -> p0 벡터
                    double v1y = p0y_arc - p1y_arc;
                    double v2x = p2x_arc - p1x_arc; // p1 -> p2 벡터
                    double v2y = p2y_arc - p1y_arc;

                    double lenV1 = Math.sqrt(v1x * v1x + v1y * v1y);
                    double lenV2 = Math.sqrt(v2x * v2x + v2y * v2y);

                    if (lenV1 > EPSILON && lenV2 > EPSILON) {
                        double dotProductNormalized = (v1x * v2x + v1y * v2y) / (lenV1 * lenV2);
                        dotProductNormalized = Math.max(-1.0, Math.min(1.0, dotProductNormalized));
                        double angle = Math.acos(dotProductNormalized); // p1에서의 각도

                        if (Math.abs(Math.sin(angle)) > EPSILON) { // 각도가 0이나 180도가 아닐 때
                            double distToTangent = CORNER_RADIUS / Math.tan(angle / 2.0);
                            double actualDistToTangent = Math.min(distToTangent, Math.min(lenV1, lenV2));
                            if (actualDistToTangent < 0) actualDistToTangent = 0; // 음수 방지
                            double actualRadius = actualDistToTangent * Math.tan(angle / 2.0);

                            if (actualRadius >= EPSILON) { // 유효한 반지름일 때 호 그리기
                                double t0x = p1x_arc + (v1x / lenV1) * actualDistToTangent; // p0-p1 선분 위 탄젠트점
                                double t0y = p1y_arc + (v1y / lenV1) * actualDistToTangent;
                                double t2x = p1x_arc + (v2x / lenV2) * actualDistToTangent; // p1-p2 선분 위 탄젠트점
                                double t2y = p1y_arc + (v2y / lenV2) * actualDistToTangent;

                                // currentX, currentY 에서 t0x, t0y 까지 직선 (H, V 또는 L 사용)
                                if (Math.abs(t0x - currentX) > EPSILON || Math.abs(t0y - currentY) > EPSILON) {
                                    if (Math.abs(t0y - currentY) < EPSILON) pathData.append(" H ").append(t0x);
                                    else if (Math.abs(t0x - currentX) < EPSILON) pathData.append(" V ").append(t0y);
                                    else pathData.append(" L ").append(t0x).append(" ").append(t0y);
                                }

                                int sweepFlag = (v1x * v2y - v1y * v2x > 0) ? 0 : 1; // 외적 기반 sweep-flag
                                int largeArcFlag = 0; // 항상 작은 호

                                pathData.append(" A ").append(actualRadius).append(" ").append(actualRadius)
                                        .append(" 0 ").append(largeArcFlag).append(" ").append(sweepFlag)
                                        .append(" ").append(t2x).append(" ").append(t2y);

                                currentX = t2x;
                                currentY = t2y;
                                i++; // p1(코너)은 처리했고, 경로는 t2(p1-p2선분 위)에서 끝남. 다음 루프는 p2부터 시작해야 함.
                                continue; // 다음 점 처리
                            }
                        }
                    }
                }
            }

            // 2. 호를 그리지 않은 경우: 직선 그리기 (H, V 최적화 시도)
            double targetX = p1.x();
            double targetY = p1.y();

            // 현재점에서 target (p1)까지 수평선인지 확인
            if (Math.abs(targetY - currentY) < EPSILON && Math.abs(targetX - currentX) > EPSILON) {
                int j = i; // svgPoints[j]가 현재 p1
                // p1 이후로도 계속 수평선인지 확인 (단, 다음 코너에서 호가 생길 경우 거기까지만)
                while (j + 1 < svgPoints.size() && Math.abs(svgPoints.get(j + 1).y() - currentY) < EPSILON) {
                    if (j + 2 < svgPoints.size() && CORNER_RADIUS > EPSILON) { // 다음 코너(svgPoints[j+1])에서 호가 생길 가능성
                        Point cornerCand = svgPoints.get(j + 1);
                        Point nextNextCand = svgPoints.get(j + 2);
                        // svgPoints[j], cornerCand, nextNextCand 가 일직선이 아니면 호가 생김 -> 수평선 연장 중단
                        boolean nextCornerIsCollinear = Math.abs((cornerCand.y() - svgPoints.get(j).y()) * (nextNextCand.x() - cornerCand.x()) -
                                (nextNextCand.y() - cornerCand.y()) * (cornerCand.x() - svgPoints.get(j).x())) < EPSILON;
                        if (!nextCornerIsCollinear) break;
                    }
                    j++; // 수평선 연장
                }
                targetX = svgPoints.get(j).x(); // 연장된 수평선의 최종 x좌표
                pathData.append(" H ").append(targetX);
                currentX = targetX; // currentY는 동일
                i = j + 1; // j까지 처리했으므로 다음 루프는 j+1부터
                continue;
            }
            // 현재점에서 target (p1)까지 수직선인지 확인
            else if (Math.abs(targetX - currentX) < EPSILON && Math.abs(targetY - currentY) > EPSILON) {
                int j = i;
                while (j + 1 < svgPoints.size() && Math.abs(svgPoints.get(j + 1).x() - currentX) < EPSILON) {
                    if (j + 2 < svgPoints.size() && CORNER_RADIUS > EPSILON) {
                        Point cornerCand = svgPoints.get(j + 1);
                        Point nextNextCand = svgPoints.get(j + 2);
                        boolean nextCornerIsCollinear = Math.abs((cornerCand.y() - svgPoints.get(j).y()) * (nextNextCand.x() - cornerCand.x()) -
                                (nextNextCand.y() - cornerCand.y()) * (cornerCand.x() - svgPoints.get(j).x())) < EPSILON;
                        if (!nextCornerIsCollinear) break;
                    }
                    j++;
                }
                targetY = svgPoints.get(j).y();
                pathData.append(" V ").append(targetY);
                currentY = targetY; // currentX는 동일
                i = j + 1;
                continue;
            }
            // 대각선이거나, 점이 같지 않은 경우 (EPSILON 고려)
            else if (Math.abs(targetX - currentX) > EPSILON || Math.abs(targetY - currentY) > EPSILON) {
                pathData.append(" L ").append(targetX).append(" ").append(targetY);
                currentX = targetX;
                currentY = targetY;
                i++;
                continue;
            }
            // 점이 같은 경우 (currentX,Y 와 p1이 동일)
            else {
                i++; // 다음 점으로 이동
                continue;
            }
        }


        if (pathData.length() > "M 0 0".length()) { // 실제로 경로 데이터가 생성되었는지 확인 (최소 M 명령어 이상)
            var visiblePath = SvgPathBuilder.path().d(pathData.toString()).fill("none").attr("marker-end", "url(#" + ARROW_HEAD_ID + ")");
            lines.add(visiblePath);
            // 투명한 상호작용 라인 (더 두껍게)
            var interactionPath = SvgPathBuilder.path().d(pathData.toString()).fill("none").stroke("transparent").strokeWidth(10); // 상호작용을 위한 두께
            lines.add(interactionPath);
        } else { // 경로 데이터가 M 명령어만 있거나 비어있으면 (이론상 M은 항상 있음)
            svg.attr("width", "0").attr("height", "0"); // 화살표를 보이지 않게
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
        BoxReferenceElement director(ValueElement start, TypeElement target);
    }
}

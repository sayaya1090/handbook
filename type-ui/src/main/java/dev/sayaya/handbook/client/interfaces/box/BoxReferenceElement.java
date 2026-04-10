package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.domain.AttributeValue;
import dev.sayaya.handbook.client.domain.Position;
import dev.sayaya.handbook.client.domain.TypeValue;
import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.arrow.Arrow;
import dev.sayaya.handbook.client.usecase.arrow.ArrowFactory;
import dev.sayaya.handbook.client.usecase.arrow.Rectangle;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static org.jboss.elemento.Elements.div;

/**
 * Document 참조 타입 간 SVG 화살표를 렌더링하는 오버레이 요소.
 *
 * <p><b>책임:</b> TypeList와 PositionMap을 구독하여 document 타입 속성의 referencedType을
 * 기반으로 타입 박스 간 방향 화살표를 SVG로 그린다. 참조 관계 변경 시 자동 갱신한다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link TypeList} — 타입 목록 구독 (속성의 document 참조 탐색)</li>
 *   <li>{@link PositionMap} — 타입 위치 구독 (화살표 좌표 계산)</li>
 *   <li>{@link ArrowFactory} — 사각형 간 최적 화살표 경로 계산</li>
 * </ul></p>
 * <p><b>주의:</b> SVG는 pointer-events:none으로 설정되어 마우스 이벤트를 투과한다.
 * 화살표 색상은 --md-sys-color-primary CSS 변수를 사용한다.</p>
 */
@Singleton
public class BoxReferenceElement implements IsElement<HTMLDivElement> {
    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private final HTMLDivElement root;
    private final Element svg;
    private final ArrowFactory arrowFactory = new ArrowFactory();
    private final TypeList typeList;
    private final PositionMap positionMap;

    @Inject
    BoxReferenceElement(TypeList typeList, PositionMap positionMap) {
        this.typeList = typeList;
        this.positionMap = positionMap;

        svg = DomGlobal.document.createElementNS(SVG_NS, "svg");
        svg.classList.add("box-reference-svg");
        svg.setAttribute("style", "position:absolute;top:0;left:0;width:100%;height:100%;overflow:visible;pointer-events:none");

        root = div().css("box-reference-container").element();
        root.appendChild(svg);

        typeList.subscribe(types -> redraw());
        positionMap.subscribe(positions -> redraw());
    }

    private void redraw() {
        // 기존 요소 삭제
        while (svg.firstChild != null) {
            svg.removeChild(svg.firstChild);
        }

        Set<TypeValue> types = typeList.getValue();
        Map<String, Position> positions = positionMap.getValue();
        if (types.isEmpty() || positions.isEmpty()) return;

        // typeId → key 매핑 (참조 타입은 id로 참조하므로)
        Map<String, String> idToKey = new HashMap<>();
        for (TypeValue type : types) {
            idToKey.put(type.id, type.key());
        }

        for (TypeValue type : types) {
            if (type.attributes == null) continue;
            Position fromPos = positions.get(type.key());
            if (fromPos == null) continue;

            for (AttributeValue attr : type.attributes) {
                if (attr.type == null || !"document".equals(attr.type.type)) continue;
                String refType = attr.type.referencedType;
                if (refType == null) continue;

                String toKey = idToKey.get(refType);
                if (toKey == null) continue;
                Position toPos = positions.get(toKey);
                if (toPos == null) continue;

                Rectangle fromRect = new Rectangle(fromPos.x, fromPos.y, fromPos.width, fromPos.height);
                Rectangle toRect = new Rectangle(toPos.x, toPos.y, toPos.width, toPos.height);
                Arrow arrow = arrowFactory.create(fromRect, toRect);

                // 선분 (단축된 끝점까지)
                Element path = DomGlobal.document.createElementNS(SVG_NS, "path");
                path.setAttribute("d", arrow.svgPath());
                path.setAttribute("fill", "none");
                path.setAttribute("stroke", "var(--md-sys-color-primary, #2196F3)");
                path.setAttribute("stroke-width", "1.5");
                svg.appendChild(path);

                // 화살표 머리 (to = 박스 테두리 위치에 직접 배치)
                Element head = createArrowHead(arrow);
                svg.appendChild(head);
            }
        }
    }

    /**
     * 화살표 머리를 to(박스 테두리) 위치에 직접 배치한다.
     * from→to 방향으로 회전된 삼각형.
     */
    private Element createArrowHead(Arrow arrow) {
        int tx = arrow.to().x();
        int ty = arrow.to().y();
        double angle = arrow.approachAngle();
        double size = 10;
        double halfWidth = 5;

        // 삼각형: tip = to, 밑변은 tip에서 -size 방향으로
        double bx = tx - size * Math.cos(angle);
        double by = ty - size * Math.sin(angle);
        double lx = bx - halfWidth * Math.sin(angle);
        double ly = by + halfWidth * Math.cos(angle);
        double rx = bx + halfWidth * Math.sin(angle);
        double ry = by - halfWidth * Math.cos(angle);

        Element polygon = DomGlobal.document.createElementNS(SVG_NS, "polygon");
        polygon.setAttribute("points",
                tx + "," + ty + " " +
                (int)lx + "," + (int)ly + " " +
                (int)rx + "," + (int)ry);
        polygon.setAttribute("fill", "var(--md-sys-color-primary, #2196F3)");
        return polygon;
    }

    @Override
    public HTMLDivElement element() { return root; }
}

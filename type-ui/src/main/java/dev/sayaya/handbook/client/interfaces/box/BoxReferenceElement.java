package dev.sayaya.handbook.client.interfaces.box;

import dev.sayaya.handbook.client.usecase.PositionMap;
import dev.sayaya.handbook.client.usecase.TypeList;
import dev.sayaya.handbook.client.usecase.TypeSearchProvider;
import dev.sayaya.handbook.client.usecase.arrow.Arrow;
import dev.sayaya.handbook.client.usecase.arrow.ArrowFactory;
import dev.sayaya.handbook.client.usecase.arrow.Rectangle;
import dev.sayaya.handbook.domain.Attribute;
import dev.sayaya.handbook.domain.Position;
import dev.sayaya.handbook.domain.Type;
import elemental2.dom.*;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.jboss.elemento.Elements.div;

/**
 * Document 참조 타입 간 SVG 화살표를 렌더링하는 오버레이 요소.
 *
 * <p><b>책임:</b> TypeSearchProvider와 PositionMap을 구독하여 
 * 현재 가시적인 타입들 간의 참조 관계를 SVG로 그린다.
 * 구버전 등 가려진 타입에서 유래한 엣지는 렌더링 대상에서 제외한다.</p>
 */
@Singleton
public class BoxReferenceElement implements IsElement<HTMLDivElement> {
    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private final HTMLDivElement root;
    private final Element svg;
    private final ArrowFactory arrowFactory = new ArrowFactory();
    private final TypeSearchProvider typeSearchProvider;
    private final PositionMap positionMap;

    @Inject
    BoxReferenceElement(TypeSearchProvider typeSearchProvider, PositionMap positionMap) {
        this.typeSearchProvider = typeSearchProvider;
        this.positionMap = positionMap;

        svg = DomGlobal.document.createElementNS(SVG_NS, "svg");
        svg.classList.add("box-reference-svg");
        svg.setAttribute("style", "position:absolute;top:0;left:0;width:100%;height:100%;overflow:visible;pointer-events:none");

        root = div().css("box-reference-container").element();
        root.appendChild(svg);

        // 이벤트 위임
        svg.addEventListener("mouseenter", e -> handleHover((MouseEvent) e, true), true);
        svg.addEventListener("mouseleave", e -> handleHover((MouseEvent) e, false), true);

        typeSearchProvider.visibleTypes().subscribe(types -> redraw());
        positionMap.subscribe(positions -> redraw());
    }

    /** 이벤트 위임 핸들러 — 개별 화살표 요소가 아닌 SVG 루트에서 처리 */
    private void handleHover(MouseEvent e, boolean highlight) {
        Element target = (Element) e.target;
        // 가장 가까운 arrow 그룹(g.box-ref-arrow)을 찾기
        Element group = findAncestor(target, "box-ref-arrow");
        if (group == null) return;
        String fromKey = group.getAttribute("data-from-key");
        String toKey = group.getAttribute("data-to-key");
        String attrName = group.getAttribute("data-attr-name");
        if (fromKey != null && toKey != null && attrName != null) {
            highlightRelation(fromKey, toKey, attrName, highlight);
        }
    }

    /** 상위 요소 중 해당 CSS 클래스를 가진 요소를 찾는다 */
    private static Element findAncestor(Element el, String className) {
        while (el != null) {
            if (el.classList != null && el.classList.contains(className)) return el;
            el = el.parentElement;
        }
        return null;
    }

    private void redraw() {
        // 기존 요소 삭제
        while (svg.firstChild != null) {
            svg.removeChild(svg.firstChild);
        }

        Set<Type> types = typeSearchProvider.getVisibleTypes();
        Map<String, Position> positions = positionMap.getValue();
        if (types.isEmpty() || positions.isEmpty()) return;

        // typeId → key 매핑 (참조 타입은 id로 참조하므로)
        Map<String, String> idToKey = new HashMap<>();
        for (Type type : types) {
            idToKey.put(type.id(), type.key());
        }

        for (Type type : types) {
            if (type.attributes() == null) continue;
            Position fromPos = positions.get(type.key());
            if (fromPos == null) continue;

            for (Attribute attr : type.attributes()) {
                if (attr.type() == null || !"document".equals(attr.type().type())) continue;
                String refType = attr.type().referencedType();
                if (refType == null) continue;

                String toKey = idToKey.get(refType);
                if (toKey == null) continue;
                Position toPos = positions.get(toKey);
                if (toPos == null) continue;

                Rectangle fromRect = new Rectangle(fromPos.x(), fromPos.y(), fromPos.width(), fromPos.height());
                Rectangle toRect = new Rectangle(toPos.x(), toPos.y(), toPos.width(), toPos.height());
                Arrow arrow = arrowFactory.create(fromRect, toRect);

                // 그룹으로 묶어 호버 이벤트 처리
                Element group = DomGlobal.document.createElementNS(SVG_NS, "g");
                group.classList.add("box-ref-arrow");
                group.setAttribute("data-from-key", type.key());
                group.setAttribute("data-to-key", toKey);
                group.setAttribute("data-attr-name", attr.name());
                group.setAttribute("style", "pointer-events:auto");

                // 투명한 넓은 히트 영역 (호버 감지용)
                Element hitArea = DomGlobal.document.createElementNS(SVG_NS, "path");
                hitArea.setAttribute("d", arrow.svgPath());
                hitArea.setAttribute("fill", "none");
                hitArea.setAttribute("stroke", "transparent");
                hitArea.setAttribute("stroke-width", "12");
                hitArea.setAttribute("style", "pointer-events:stroke;cursor:pointer");
                group.appendChild(hitArea);

                // 실제 선분
                Element path = DomGlobal.document.createElementNS(SVG_NS, "path");
                path.classList.add("box-ref-line");
                path.setAttribute("d", arrow.svgPath());
                path.setAttribute("fill", "none");
                path.setAttribute("stroke", "var(--md-sys-color-primary, #2196F3)");
                path.setAttribute("stroke-width", "1.5");
                group.appendChild(path);

                // 화살표 머리
                Element head = createArrowHead(arrow);
                head.classList.add("box-ref-head");
                group.appendChild(head);

                // 이벤트는 SVG 루트에서 위임 처리 (handleHover) — 개별 리스너 불필요
                svg.appendChild(group);
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

    private void highlightRelation(String fromKey, String toKey, String attrName, boolean highlight) {
        // 화살표 자체 하이라이트
        elemental2.dom.NodeList arrows = svg.querySelectorAll(
            "g[data-from-key='" + fromKey + "'][data-attr-name='" + attrName + "']");
        for (int i = 0; i < arrows.length; i++) {
            Element g = (Element) arrows.item(i);
            g.classList.toggle("box-ref-hover", highlight);
        }

        // 참조받는 타입 박스 하이라이트
        HTMLElement toBox = (HTMLElement) DomGlobal.document.querySelector(
            ".type-box[data-type-key='" + toKey + "']");
        if (toBox != null) toBox.classList.toggle("ref-highlight-target", highlight);

        // 참조하는 속성 행 하이라이트
        HTMLElement fromBox = (HTMLElement) DomGlobal.document.querySelector(
            ".type-box[data-type-key='" + fromKey + "']");
        if (fromBox != null) {
            elemental2.dom.NodeList rows = fromBox.querySelectorAll(".type-attr-row");
            for (int j = 0; j < rows.length; j++) {
                HTMLElement row = (HTMLElement) rows.item(j);
                HTMLElement nameEl = (HTMLElement) row.querySelector(".type-attr-name");
                if (nameEl != null && attrName.equals(nameEl.textContent)) {
                    row.classList.toggle("ref-highlight-source", highlight);
                }
            }
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}

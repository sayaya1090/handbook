package dev.sayaya.handbook.client.usecase.arrow;

/**
 * 두 사각형(타입 박스) 사이의 SVG 화살표 경로를 계산한다.
 *
 * <p><b>책임:</b> 도착점이 실제로 접하는 변(top/bottom/left/right)을 감지하여
 * 화살표 머리가 해당 변에 직각으로 향하도록 approachAngle을 결정한다.
 * 곡선 종단점은 화살표 삼각형 밑변(tip에서 ARROW_HEAD_LENGTH 뒤)이다.</p>
 *
 * <p><b>주의:</b> 곡선의 제어점은 접하는 변의 법선 방향으로 배치되어
 * 곡선이 자연스럽게 변에 수직으로 도착한다.</p>
 */
public class ArrowFactory {
    private static final int ARROW_HEAD_LENGTH = 10;

    /** 두 박스 사이의 화살표를 생성한다. from → to 방향. */
    public Arrow create(Rectangle from, Rectangle to) {
        Point start = nearestBorderPoint(from, to.center());
        Point end = nearestBorderPoint(to, from.center());

        // 도착점이 to 박스의 어느 변에 접하는지 감지
        Side endSide = detectSide(to, end);
        // 출발점이 from 박스의 어느 변에 접하는지 감지
        Side startSide = detectSide(from, start);

        // 화살표 머리 방향: 접하는 변에 직각으로 안쪽을 향함
        double approachAngle = endSide.inwardAngle();

        // 곡선 종단점: 변에서 ARROW_HEAD_LENGTH만큼 뒤로
        Point shortenedEnd = endSide.offset(end, ARROW_HEAD_LENGTH);

        // 곡선 제어점: 출발 변 법선 + 도착 변 법선 방향으로 배치
        String path = buildCurve(start, startSide, shortenedEnd, endSide);

        return new Arrow(start, end, approachAngle, path);
    }

    /** 3차 베지어 곡선: 출발/도착 변의 법선 방향으로 제어점 배치 */
    private String buildCurve(Point start, Side startSide, Point end, Side endSide) {
        int dx = end.x() - start.x();
        int dy = end.y() - start.y();
        int dist = (int) Math.sqrt(dx * dx + dy * dy);
        int ctrlDist = Math.max(dist / 3, 30);

        // 출발 제어점: 출발 변의 바깥 법선 방향 (변에서 멀어지는 쪽)
        Point cp1 = startSide.offset(start, ctrlDist);
        // 도착 제어점: 도착 변의 바깥 법선 방향
        Point cp2 = endSide.offset(end, ctrlDist);

        return "M " + start.x() + " " + start.y()
             + " C " + cp1.x() + " " + cp1.y()
             + ", " + cp2.x() + " " + cp2.y()
             + ", " + end.x() + " " + end.y();
    }

    /** 점이 사각형의 어느 변에 접하는지 감지한다. */
    private Side detectSide(Rectangle rect, Point p) {
        int tolerance = 2;
        if (Math.abs(p.y() - rect.y()) <= tolerance) return Side.TOP;
        if (Math.abs(p.y() - (rect.y() + rect.height())) <= tolerance) return Side.BOTTOM;
        if (Math.abs(p.x() - rect.x()) <= tolerance) return Side.LEFT;
        if (Math.abs(p.x() - (rect.x() + rect.width())) <= tolerance) return Side.RIGHT;
        // 폴백: 가장 가까운 변
        int dTop = Math.abs(p.y() - rect.y());
        int dBottom = Math.abs(p.y() - (rect.y() + rect.height()));
        int dLeft = Math.abs(p.x() - rect.x());
        int dRight = Math.abs(p.x() - (rect.x() + rect.width()));
        int min = Math.min(Math.min(dTop, dBottom), Math.min(dLeft, dRight));
        if (min == dTop) return Side.TOP;
        if (min == dBottom) return Side.BOTTOM;
        if (min == dLeft) return Side.LEFT;
        return Side.RIGHT;
    }

    private Point nearestBorderPoint(Rectangle rect, Point target) {
        int cx = rect.x() + rect.width() / 2;
        int cy = rect.y() + rect.height() / 2;
        int dx = target.x() - cx;
        int dy = target.y() - cy;
        if (dx == 0 && dy == 0) return new Point(cx, cy);

        double scaleX = Math.abs(dx) > 0 ? (rect.width() / 2.0) / Math.abs(dx) : Double.MAX_VALUE;
        double scaleY = Math.abs(dy) > 0 ? (rect.height() / 2.0) / Math.abs(dy) : Double.MAX_VALUE;
        double scale = Math.min(scaleX, scaleY);

        return new Point(cx + (int)(dx * scale), cy + (int)(dy * scale));
    }

    /** 사각형의 변. 법선 방향과 오프셋 계산을 캡슐화한다. */
    enum Side {
        TOP {
            @Override double inwardAngle() { return Math.PI * 0.5; } // 아래를 향함
            @Override Point offset(Point p, int dist) { return new Point(p.x(), p.y() - dist); }
        },
        BOTTOM {
            @Override double inwardAngle() { return Math.PI * 1.5; } // 위를 향함
            @Override Point offset(Point p, int dist) { return new Point(p.x(), p.y() + dist); }
        },
        LEFT {
            @Override double inwardAngle() { return 0; } // 오른쪽을 향함
            @Override Point offset(Point p, int dist) { return new Point(p.x() - dist, p.y()); }
        },
        RIGHT {
            @Override double inwardAngle() { return Math.PI; } // 왼쪽을 향함
            @Override Point offset(Point p, int dist) { return new Point(p.x() + dist, p.y()); }
        };

        /** 변 안쪽을 향하는 법선 각도 (라디안). 화살표 머리가 이 방향으로 회전된다. */
        abstract double inwardAngle();
        /** 변의 법선 방향으로 dist만큼 이동한 점. 양수 = 안쪽, 음수 = 바깥쪽. */
        abstract Point offset(Point p, int dist);
    }
}

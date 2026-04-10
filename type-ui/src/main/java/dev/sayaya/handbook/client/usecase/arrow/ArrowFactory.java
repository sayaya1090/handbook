package dev.sayaya.handbook.client.usecase.arrow;

/**
 * 두 사각형(타입 박스) 사이의 SVG 화살표 경로를 계산한다.
 * 최단 거리의 테두리 포인트를 기준으로 곡선 경로를 생성한다.
 */
public class ArrowFactory {
    private static final int ARROW_HEAD_LENGTH = 10;

    /** 두 박스 사이의 화살표를 생성한다. from → to 방향. */
    public Arrow create(Rectangle from, Rectangle to) {
        Point start = nearestBorderPoint(from, to.center());
        Point end = nearestBorderPoint(to, from.center());

        int dx = end.x() - start.x();
        int dy = end.y() - start.y();
        boolean horizontal = Math.abs(dx) >= Math.abs(dy);

        // 곡선 접근 방향(수평 또는 수직)을 따라 end를 단축
        Point shortenedEnd;
        double approachAngle;
        String path;

        if (horizontal) {
            int dir = dx > 0 ? -1 : 1; // end에서 start 쪽으로
            shortenedEnd = new Point(end.x() + dir * ARROW_HEAD_LENGTH, end.y());
            approachAngle = dir > 0 ? Math.PI : 0; // 접근 방향 = end 쪽을 향함
            int midX = (start.x() + shortenedEnd.x()) / 2;
            path = "M " + start.x() + " " + start.y()
                 + " C " + midX + " " + start.y()
                 + ", " + midX + " " + shortenedEnd.y()
                 + ", " + shortenedEnd.x() + " " + shortenedEnd.y();
        } else {
            int dir = dy > 0 ? -1 : 1;
            shortenedEnd = new Point(end.x(), end.y() + dir * ARROW_HEAD_LENGTH);
            approachAngle = dir > 0 ? Math.PI * 1.5 : Math.PI * 0.5; // 위→아래 or 아래→위
            int midY = (start.y() + shortenedEnd.y()) / 2;
            path = "M " + start.x() + " " + start.y()
                 + " C " + start.x() + " " + midY
                 + ", " + shortenedEnd.x() + " " + midY
                 + ", " + shortenedEnd.x() + " " + shortenedEnd.y();
        }

        return new Arrow(start, end, approachAngle, path);
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
}

package dev.sayaya.handbook.client.usecase.arrow;

import java.util.List;

public record Arrow(
        List<Point> pathPoints  // 화살표를 구성하는 모든 점들의 리스트
) {}
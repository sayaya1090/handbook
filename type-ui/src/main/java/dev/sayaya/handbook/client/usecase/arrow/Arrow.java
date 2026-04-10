package dev.sayaya.handbook.client.usecase.arrow;

/**
 * SVG 화살표 경로. Document 참조 타입 간 연결선.
 * @param from 출발 박스 테두리 좌표
 * @param to 도착 박스 테두리 좌표 (화살표 머리 tip)
 * @param approachAngle 도착점에서의 접근 각도 (라디안, 화살표 머리 방향)
 * @param svgPath SVG path 문자열 (선분, 화살표 머리 직전까지)
 */
public record Arrow(Point from, Point to, double approachAngle, String svgPath) {}

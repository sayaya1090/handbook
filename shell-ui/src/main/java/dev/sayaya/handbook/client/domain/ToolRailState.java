package dev.sayaya.handbook.domain;

/**
 * Tool rail 의 가시성 상태. 뷰포트(모바일/데스크톱) 와 직교하며,
 * 모바일 여부는 {@code .rail[mobile]} 속성으로 별도 표현된다.
 */
public enum ToolRailState {
    COLLAPSE, EXPAND, HIDE
}

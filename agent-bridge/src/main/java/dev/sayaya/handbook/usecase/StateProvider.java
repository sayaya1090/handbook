package dev.sayaya.handbook.usecase;

/**
 * 현재 편집 중인 모듈의 상태를 JSON 문자열로 제공한다.
 * 에이전트가 현재 캔버스 상태를 읽어 지능적인 mutation을 생성할 수 있도록 한다.
 */
public interface StateProvider {
    /** 현재 편집 상태를 JSON 문자열로 반환한다. */
    String snapshot();
}

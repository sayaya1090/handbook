package dev.sayaya.handbook.usecase;

import dev.sayaya.rx.Observable;

/**
 * 에이전트가 데이터를 검색할 수 있는 포트.
 * 각 프론트엔드 모듈이 구현하여 에이전트에게 현재 상태나 서버 데이터를 제공한다.
 *
 * <p>에이전트는 MutateCommand를 보내기 전에 이 인터페이스로 현재 상태를 조회하여
 * 정확한 명령을 생성할 수 있다.
 */
public interface SearchProvider {
    /**
     * 검색 쿼리를 실행하고 결과를 JSON 문자열로 반환한다.
     * @param query 검색 쿼리 (모듈별로 해석)
     * @return JSON 문자열 Observable
     */
    Observable<String> search(String query);
}

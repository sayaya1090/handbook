package dev.sayaya.handbook.domain;

/**
 * 검색 시각화 요청 값 객체.
 *
 * <p><b>책임:</b> search 커맨드의 검색 대상 페이지(navigateTo), 검색 쿼리(query),
 * 매칭 결과 셀렉터 목록(targets), 결과 설명(summary)을 UI에 전달한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 VO)</li></ul></p>
 */
public class SearchVisualizationRequest {
    private final String navigateTo;
    private final String query;
    private final String[] targets;
    private final String summary;

    public SearchVisualizationRequest(String navigateTo, String query, String[] targets, String summary) {
        this.navigateTo = navigateTo;
        this.query = query;
        this.targets = targets != null ? targets : new String[0];
        this.summary = summary;
    }

    /** 검색 결과를 보여줄 대상 페이지 URL (navigate 커맨드 대상) */
    public String navigateTo() { return navigateTo; }
    /** 사용자가 요청한 검색 쿼리 */
    public String query() { return query; }
    /** 검색 결과로 하이라이트할 CSS 셀렉터 목록 */
    public String[] targets() { return targets; }
    /** 검색 결과 요약 설명 */
    public String summary() { return summary; }
}

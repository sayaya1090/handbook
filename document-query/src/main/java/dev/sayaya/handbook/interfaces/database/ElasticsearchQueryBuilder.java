package dev.sayaya.handbook.interfaces.database;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.util.UUID;

/**
 * Elasticsearch 검색 쿼리를 생성하는 정적 빌더 유틸리티.
 *
 * <p><b>책임:</b> 코틀린 컴파일러의 복잡한 제네릭 타입 추론 오류를 방지하기 위해 Java로 작성되었다.
 * 워크스페이스 필터링이 포함된 전문 검색(Full-text search) NativeQuery를 생성한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>Elasticsearch Java Client (co.elastic.clients.elasticsearch)</li>
 *   <li>Spring Data Elasticsearch (NativeQuery)</li>
 * </ul></p>
 */
public class ElasticsearchQueryBuilder {
    public static NativeQuery buildFullTextNativeQuery(UUID workspace, String queryText, Pageable pageable) {
        QueryStringQuery queryString = new QueryStringQuery.Builder()
                .query(queryText)
                .fields("data.*")
                .build();
        
        TermQuery workspaceFilter = new TermQuery.Builder()
                .field("workspace")
                .value(workspace.toString())
                .build();

        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(queryString._toQuery())
                .filter(workspaceFilter._toQuery())
                .build();

        return NativeQuery.builder()
                .withQuery(boolQuery._toQuery())
                .withPageable(pageable)
                .build();
    }
}

package dev.sayaya.handbook.interfaces.database;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.util.UUID;

/**
 * 코틀린의 타입 추론 오류를 피하기 위한 Java 기반 쿼리 빌더 유틸리티.
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

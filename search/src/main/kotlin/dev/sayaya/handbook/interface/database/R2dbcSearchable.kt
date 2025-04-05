package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.usecase.Searchable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.sql.SqlIdentifier
import reactor.core.publisher.Mono

interface R2dbcSearchable<E: EntityPageable, T>: Searchable<T> {
    fun R2dbcEntityTemplate.predicates(filters: List<Pair<String, Any?>>): Criteria {
        if(filters.isEmpty()) return Criteria.empty()
        return filters.map { (key, value) -> predicate(key, value) }.reduce(Criteria::and)
    }
    fun R2dbcEntityTemplate.predicate(key: String, value: Any?): Criteria
    fun R2dbcEntityTemplate.search(from: SqlIdentifier, filters: List<Pair<String, Any?>>, clazz: Class<E>,  pageable: Pageable): Mono<Page<E>> = predicates(filters)
        .let(Query::query).with(pageable).columns("*, count(*) OVER() as count").let { query ->
            select(clazz).from(from).`as`(clazz).matching(query)
        }.all().collectList().map { list ->
            if (list.isEmpty()) PageImpl(emptyList<E>(), pageable, 0)
            else PageImpl(list, pageable, list.first().count)
        }
}
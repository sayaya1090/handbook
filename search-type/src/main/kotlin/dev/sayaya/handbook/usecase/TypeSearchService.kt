package dev.sayaya.handbook.usecase

import dev.sayaya.domain.Search
import dev.sayaya.handbook.domain.Type
import dev.sayaya.usecase.Searchable
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TypeSearchService(private val repo: TypeSearchRepository): Searchable<Type> {
    override fun search(param: Search): Mono<Page<Type>> = repo.search(param)
}
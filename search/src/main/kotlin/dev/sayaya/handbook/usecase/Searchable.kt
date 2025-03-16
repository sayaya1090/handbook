package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Search
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono

interface Searchable<T> {
    fun search(param: Search): Mono<Page<T>>
}
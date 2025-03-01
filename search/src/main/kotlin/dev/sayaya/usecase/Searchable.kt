package dev.sayaya.usecase

import dev.sayaya.domain.Search
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono

interface Searchable<T> {
    fun search(param: Search): Mono<Page<T>>
}
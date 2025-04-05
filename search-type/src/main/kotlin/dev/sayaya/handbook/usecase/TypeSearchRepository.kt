package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.domain.Type
import org.springframework.data.domain.Page
import reactor.core.publisher.Mono
import java.util.*

interface TypeSearchRepository {
    fun search(workspace: UUID, param: Search): Mono<Page<Type>>
}
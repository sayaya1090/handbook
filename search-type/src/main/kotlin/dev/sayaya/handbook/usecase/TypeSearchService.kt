package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Search
import dev.sayaya.handbook.domain.Type
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class TypeSearchService(private val repo: TypeSearchRepository) {
    fun search(workspace: UUID, param: Search): Mono<Page<Type>> = param.copy (
        filters = param.filters.let { filters ->
            if (filters.none { (key, _) -> key == "date" })
                filters + ("date" to Instant.now().toEpochMilli().toString()) else filters
        }
    ).let { search -> repo.search(workspace, search) }
}
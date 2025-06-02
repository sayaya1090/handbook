package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.TypeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate) : TypeRepository {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    @CachePut(value = ["type"], key = "#workspace + ':' + #type.id + ':' + #type.version")
    override fun cache(workspace: UUID, type: Type): Mono<Void> {
        log.info("Caching type: workspace={}, id={}, version={}", workspace, type.id, type.version)
        return Mono.empty()
    }
    @Cacheable(value = ["type"], key = "#workspace + ':' + #id + ':' + #version")
    override fun find(workspace: UUID, id: String, version: String): Mono<Void> {
        log.info("Finding type: workspace={}, id={}, version={}", workspace, id, version)
        return Mono.empty()
    }
}

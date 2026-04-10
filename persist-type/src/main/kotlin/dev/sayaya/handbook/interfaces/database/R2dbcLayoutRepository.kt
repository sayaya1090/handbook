package dev.sayaya.handbook.interfaces.database

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.TypeLayout
import dev.sayaya.handbook.usecase.LayoutRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface R2dbcLayoutEntityRepository : ReactiveCrudRepository<R2dbcLayoutEntity, UUID> {
    fun findByWorkspace(workspace: UUID): Flux<R2dbcLayoutEntity>
}

class R2dbcLayoutRepositoryAdapter(
    private val repository: R2dbcLayoutEntityRepository,
    private val objectMapper: ObjectMapper,
) : LayoutRepository {

    override fun findByWorkspace(workspace: UUID): Flux<TypeLayout> =
        repository.findByWorkspace(workspace).map { entity ->
            val positions: Map<String, TypeLayout.Position> = entity.positions?.let {
                objectMapper.readValue(it)
            } ?: emptyMap()
            entity.toDomain(positions)
        }

    override fun save(workspace: UUID, layout: TypeLayout): Mono<TypeLayout> {
        val positionsJson = objectMapper.writeValueAsString(layout.positions)
        val entity = R2dbcLayoutEntity.fromDomain(layout, positionsJson)
        return repository.save(entity).map { saved ->
            val positions: Map<String, TypeLayout.Position> = saved.positions?.let {
                objectMapper.readValue(it)
            } ?: emptyMap()
            saved.toDomain(positions)
        }
    }
}

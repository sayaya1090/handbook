package dev.sayaya.handbook.interfaces.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface R2dbcAttributeEntityRepository : ReactiveCrudRepository<R2dbcAttributeEntity, UUID> {
    fun findByTypeIdAndTypeVersion(typeId: String, typeVersion: String): Flux<R2dbcAttributeEntity>

    fun findByWorkspaceAndTypeIdIn(workspace: UUID, typeIds: Collection<String>): Flux<R2dbcAttributeEntity>

    @Query("DELETE FROM type_attributes WHERE type_id = :typeId AND type_version = :typeVersion")
    fun deleteByTypeIdAndTypeVersion(typeId: String, typeVersion: String): Mono<Void>

    @Query("DELETE FROM type_attributes WHERE type_id = :typeId AND type_version = :typeVersion AND name = :name")
    fun deleteByTypeIdAndTypeVersionAndName(typeId: String, typeVersion: String, name: String): Mono<Void>
}

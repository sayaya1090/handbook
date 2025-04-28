package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeRepository
import io.r2dbc.spi.Statement
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.time.Instant
import java.util.*

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate, private val childRepo: R2dbcAttributeRepository): TypeRepository {
    fun save(workspace: UUID, type: Type): Mono<Type> = insert(workspace, type).flatMap { entity ->
        persistAttributes(entity, type).map(this::toDomain)
    }
    private fun insert(workspace: UUID, type: Type): Mono<R2dbcTypeEntity> = R2dbcTypeEntity.of(
        workspace = workspace,
        id = Ulid.fast().toUuid(),
        name = type.id, version = type.version, parent = type.parent,
        effectiveDateTime = type.effectDateTime, expiryDateTime =  type.expireDateTime,
        description = type.description ?: "",
        primitive = type.primitive
    ).let(template::insert)
    private fun persistAttributes(entity: R2dbcTypeEntity, type: Type): Mono<Tuple2<R2dbcTypeEntity, List<Attribute>>> = childRepo.save(entity, type.attributes)
        .map { Tuples.of(entity, it) }
    private fun toDomain(tuple: Tuple2<R2dbcTypeEntity, List<Attribute>>): Type = toDomain(tuple.t1, tuple.t2)
    private fun toDomain(entity: R2dbcTypeEntity, attributes: List<Attribute>): Type = Type (
        id = entity.name,
        parent = entity.parent,
        version = entity.version,
        effectDateTime = entity.effectDateTime,
        expireDateTime = entity.expireDateTime,
        description = entity.description,
        primitive = entity.primitive,
        attributes = attributes
    )

    override fun saveAll(workspace: UUID, types: List<Type>): Mono<List<Type>> = types.toEntity(workspace).let { entities ->
        template.saveAll(entities)
    }.thenReturn(types)

    // 배치를 한번에 저장한다.
    private fun R2dbcEntityTemplate.saveAll(entities: List<R2dbcTypeEntity>): Mono<Void> = getAuthenticatedUser().flatMap { user->
        val now = Instant.now() // 현재 시각
        val sql = """
                INSERT INTO type (
                    workspace, id, name, version, parent, 
                    effective_at, expire_at, 
                    description, primitive,
                    created_by, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        databaseClient.inConnection { connection ->
            val statement = connection.createStatement(sql)
            entities.forEach { entity -> statement.bind(entity, user, now).add() }
            Flux.from(statement.execute()).then()
        }
    }
    private fun getAuthenticatedUser(): Mono<String> = ReactiveSecurityContextHolder.getContext().map { it.authentication.name }
        .switchIfEmpty(Mono.error(IllegalStateException("Unable to retrieve authenticated user context")))
    private fun Statement.bind(entity: R2dbcTypeEntity, user: String, now: Instant) = bind(0, entity.workspace)
        .bind(1, entity.id)
        .bind(2, entity.name)
        .bind(3, entity.version)
        .bind(4, entity.parent)
        .bind(5, entity.effectDateTime)
        .bind(6, entity.expireDateTime)
        .bind(7, entity.description)
        .bind(8, entity.primitive)
        .bind(9, user)
        .bind(10, now)
    private fun List<Type>.toEntity(workspace: UUID) = this.map { type ->
        R2dbcTypeEntity.of(
            workspace = workspace,
            id = Ulid.fast().toUuid(),
            name = type.id, version = type.version, parent = type.parent,
            effectiveDateTime = type.effectDateTime, expiryDateTime =  type.expireDateTime,
            description = type.description ?: "",
            primitive = type.primitive
        )
    }
}
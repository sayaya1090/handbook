package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.time.Instant
import java.util.*

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate, private val childRepo: R2dbcAttributeRepository): TypeRepository, BatchRepository<R2dbcTypeEntity> {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
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

    override fun saveAll(workspace: UUID, types: List<Type>): Mono<List<Type>> = saveAll(types.toEntity(workspace)).thenReturn(types)
    // 배치를 한번에 저장한다.
    private fun saveAll(entities: List<R2dbcTypeEntity>): Mono<List<R2dbcTypeEntity>> = getAuthenticatedUser().flatMap { user->
        val now = Instant.now() // 현재 시각
        saveAll(entities, escapeSql(user), formatTimestampLiteral(now))
            .collectList()
            .doOnError { error -> log.error("Batch execution failed inside inConnection!", error) } // 에러 로깅
    }
    private fun getAuthenticatedUser(): Mono<String> = ReactiveSecurityContextHolder.getContext().map { it.authentication.name }
        .switchIfEmpty(Mono.error(IllegalStateException("Unable to retrieve authenticated user context")))

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

    override val databaseClient: DatabaseClient = template.databaseClient
    override fun insertInto(): String = INSERT_TYPE_SQL
    override fun R2dbcTypeEntity.toCsv(): String = """
        ${escapeSql(workspace.toString())},
        ${escapeSql(id.toString())},
        ${escapeSql(name)},
        ${escapeSql(version)},
        ${escapeSql(parent)},
        ${formatTimestampLiteral(effectDateTime)},
        ${formatTimestampLiteral(expireDateTime)},
        ${escapeSql(description)},
        $primitive
    """.trimIndent()

    companion object {
        private const val INSERT_TYPE_SQL = """
            INSERT INTO type (workspace, id, name, version, parent, effective_at, expire_at, 
            description, primitive, created_by, created_at) 
        """
    }
}
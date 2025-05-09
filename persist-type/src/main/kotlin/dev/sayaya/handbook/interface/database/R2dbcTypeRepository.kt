package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
class R2dbcTypeRepository(override val databaseClient: DatabaseClient): TypeRepository, R2dbcTypeBatchUpsertRepository, R2dbcTypeBatchDeleteRepository {
    override val log: Logger = LoggerFactory.getLogger(this::class.java)
    override fun saveAll(workspace: UUID, types: List<Type>): Mono<List<Type>> = getAuthenticatedUser().flatMapMany { user ->
        saveAll(types.toEntity(workspace), user, Instant.now())
    }.collectList().doOnError { error ->
        log.error("Batch execution failed inside inConnection!", error)
    }.thenReturn(types)

    override fun deleteAll(workspace: UUID, types: List<Type>): Mono<List<Type>> = deleteAll(types.toEntity(workspace)).
    collectList().doOnError { error ->
        log.error("Batch execution failed inside inConnection!", error)
    }.thenReturn(types)

    private fun getAuthenticatedUser(): Mono<String> = ReactiveSecurityContextHolder.getContext().map { it.authentication.name }
        .switchIfEmpty(Mono.error(IllegalStateException("Unable to retrieve authenticated user context")))

    private fun List<Type>.toEntity(workspace: UUID) = map { type ->
        R2dbcTypeEntity.of(
            workspace = workspace,
            id = Ulid.fast().toUuid(),
            name = type.id, version = type.version, parent = type.parent,
            effectiveDateTime = type.effectDateTime, expiryDateTime =  type.expireDateTime,
            description = type.description ?: "",
            primitive = type.primitive,
            x = type.x, y = type.y, width = type.width, height = type.height
        ).apply {
            attributes = type.attributes.toEntity(workspace, id)
        }
    }
    private fun List<Attribute>.toEntity(workspace: UUID, typeId: UUID) = map { attribute ->
        R2dbcAttributeEntity(
            workspace = workspace,
            type = typeId,
            name = attribute.name,
            order = attribute.order,
            attributeType = attribute.type,
            keyType = if(attribute is Attribute.Companion.HasKeyType) attribute.keyType else null,
            valueType = if(attribute is Attribute.Companion.HasValueType) attribute.valueType else null,
            referenceType = if(attribute is Attribute.Companion.DocumentAttribute) attribute.referenceType else null,
            fileExtensions = if(attribute is Attribute.Companion.FileAttribute) attribute.extensions.joinToString(",") else null,
            description = attribute.description,
            nullable = attribute.nullable
        )
    }
}
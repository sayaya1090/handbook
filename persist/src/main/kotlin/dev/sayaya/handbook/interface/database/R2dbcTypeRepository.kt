package dev.sayaya.handbook.`interface`.database

import com.github.f4b6a3.ulid.Ulid
import dev.sayaya.handbook.domain.Attribute
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.util.*

@Repository
class R2dbcTypeRepository(private val template: R2dbcEntityTemplate, private val childRepo: R2dbcAttributeRepository): TypeRepository {
    @Transactional
    override fun save(workspace: UUID, type: Type): Mono<Type> = insert(workspace, type).flatMap { entity ->
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
}
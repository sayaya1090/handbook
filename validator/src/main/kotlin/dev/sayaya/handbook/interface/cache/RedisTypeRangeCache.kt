package dev.sayaya.handbook.`interface`.cache

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Type
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Component
class RedisTypeRangeCache (
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
): TypeRangeCache {
    private val zSetOps = reactiveRedisTemplate.opsForZSet()
    private fun typeDataKey(workspace: UUID, id: String): String = "type:data:${workspace}:${id}"
    private fun effectZsetKey(workspace: UUID, id: String): String = "type:effect:zset:${workspace}:${id}"
    private fun expireZsetKey(workspace: UUID, id: String): String = "type:expire:zset:${workspace}:${id}"

    override fun saveType(workspace: UUID, type: Type): Mono<Void> {
        val typeKey = typeDataKey(workspace, type.id)
        val serializedType = try {
            objectMapper.writeValueAsString(type)
        } catch (e: Exception) {
            return Mono.error(IllegalStateException("Failed to serialize Type object: ${type.id}", e))
        }

        val effectScore = type.effectDateTime.toEpochMilli().toDouble()
        val expireScore = type.expireDateTime.toEpochMilli().toDouble()

        return reactiveRedisTemplate.opsForValue().set(typeKey, serializedType)
            .then(zSetOps.add(effectZsetKey(workspace, type.id), typeKey, effectScore))
            .then(zSetOps.add(expireZsetKey(workspace, type.id), typeKey, expireScore))
            .then()
    }
    override fun findTypesByTimeRange(workspace: UUID, id: String, startInstant: Instant, endInstant: Instant): Mono<Type> {
        // effectDateTime은 endInstant 이하여야 합니다.
        // 즉, [minScore, endInstant.toEpochMilli()] 범위입니다.
        val effectRange = Range.closed(Double.MIN_VALUE, endInstant.toEpochMilli().toDouble()) // 또는 Range.open(Double.NEGATIVE_INFINITY, endInstant.toEpochMilli().toDouble())

        // expireDateTime은 startInstant 이상이어야 합니다.
        // 즉, [startInstant.toEpochMilli(), maxScore] 범위입니다.
        val expireRange = Range.closed(startInstant.toEpochMilli().toDouble(), Double.MAX_VALUE) // 또는 Range.open(startInstant.toEpochMilli().toDouble(), Double.POSITIVE_INFINITY)

        // workspace별로 ZSET 사용
        val effectMatches = zSetOps.rangeByScore(effectZsetKey(workspace, id), effectRange)
        val expireMatches = zSetOps.rangeByScore(expireZsetKey(workspace, id), expireRange)
        return Flux.zip(effectMatches.collectList(), expireMatches.collectList())
            .flatMap { tuple ->
                val commonKeys = tuple.t1.intersect(tuple.t2) // 교집합 찾기
                Flux.fromIterable(commonKeys)
            }.flatMap { typeKey ->
                reactiveRedisTemplate.opsForValue().get(typeKey).mapNotNull { serializedType ->
                    objectMapper.readValue(serializedType, Type::class.java) // Type 객체 역직렬화
                }
            }.singleOrEmpty()
    }
    override fun clearCache(workspace: UUID, id: String): Mono<Void> {
        val typeDataPattern = typeDataKey(workspace, id)
        return reactiveRedisTemplate.keys(typeDataPattern)
            .flatMap { key -> reactiveRedisTemplate.delete(key) }
            .then(reactiveRedisTemplate.delete(effectZsetKey(workspace, id)))
            .then(reactiveRedisTemplate.delete(expireZsetKey(workspace, id)))
            .then()
    }

}
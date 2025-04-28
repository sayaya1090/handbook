package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class LayoutFactory(private val repo: LayoutRepository) {
    private val layoutIdCache = ConcurrentHashMap<CacheKey, UUID>()
    fun getOrCreateLayouts(workspace: UUID, types: List<Type>): Mono<Layout> {
        if (types.isEmpty()) return Mono.empty()
        val (effectiveDate, expireDate) = try {
            calculateDateIntersection(types)
        } catch(e: IllegalStateException) {
            return Mono.error(e)
        }
        val layoutId = getOrComputeLayoutId(workspace, types)
        return repo.findById(workspace, layoutId)
            .switchIfEmpty(Mono.defer {
                Mono.just(createLayout(workspace, layoutId))
            }).flatMap { layout ->
                if(layout.isInitialized() && effectiveDate==layout.effectDateTime && expireDate==layout.expireDateTime) layout.toMono()
                else layout.apply {
                    this.effectDateTime = effectiveDate
                    this.expireDateTime = expireDate
                }.let(repo::save)
            }
    }
    private fun getOrComputeLayoutId(workspace: UUID, types: List<Type>): UUID {
        if (types.size <= 2) return generateLayoutId(workspace, types)
        val key = CacheKey(workspace, types.map { TypeKey(it.id, it.version) }.sortedBy { it.toString() })
        return layoutIdCache.computeIfAbsent(key) { generateLayoutId(workspace, types) }
    }
    private fun generateLayoutId(workspace: UUID, types: List<Type>): UUID {
        val key = buildString {
            append(workspace.toString())
            append(':')
            types.asSequence()
                .map { "${it.id}@${it.version}" }
                .sorted()
                .joinTo(this, separator = SEPARATOR)
        }
        val messageDigest = MESSAGE_DIGEST.get().also { it.reset() }
        val hashBytes = messageDigest.digest(key.toByteArray())
        val bytes = ByteBuffer.wrap(hashBytes, 0, 16)
        val msb = bytes.long
        val lsb = bytes.long
        return UUID(
            (msb and VERSION_CLEAR_MASK) or VERSION_BITS,
            (lsb and VARIANT_CLEAR_MASK) or VARIANT_BITS
        )
    }
    private fun createLayout(workspace: UUID, id: UUID): Layout = Layout(workspace, id)
    private fun calculateDateIntersection(types: List<Type>): Pair<Instant, Instant> {
        // 가장 늦은 시작일(최대 effective date)과 가장 이른 종료일(최소 expire date) 찾기
        var latestEffectiveDate = Instant.MIN
        var earliestExpireDate = Instant.MAX
        // 모든 타입의 유효 시작일과 종료일 중 각각 최대값과 최소값 찾기
        for (type in types) {
            // 타입의 유효 시작일이 없으면 가장 과거로 간주
            val typeEffectiveDate = type.effectDateTime
            if (typeEffectiveDate.isAfter(latestEffectiveDate)) {
                latestEffectiveDate = typeEffectiveDate
            }
            // 타입의 유효 종료일이 없으면 가장 미래로 간주
            val typeExpireDate = type.expireDateTime
            if (typeExpireDate.isBefore(earliestExpireDate)) {
                earliestExpireDate = typeExpireDate
            }
        }
        // 유효 기간 검증: 시작일이 종료일보다 이후면 유효한 교집합이 없음
        check(earliestExpireDate.isAfter(latestEffectiveDate)) {
            "유효 기간이 교차하지 않습니다. 시작일: $latestEffectiveDate, 종료일: $earliestExpireDate"
        }
        return Pair(latestEffectiveDate, earliestExpireDate)
    }


    companion object {
        private val MESSAGE_DIGEST = ThreadLocal.withInitial {
            MessageDigest.getInstance(HASH_ALGORITHM)
        }
        // 알고리즘 관련 상수
        private const val HASH_ALGORITHM = "SHA-256"
        private const val SEPARATOR = "$"

        // UUID 버전 관련 상수
        private const val VERSION_BITS = 0x4000L
        private const val VERSION_CLEAR_MASK = -0x1000L

        // UUID 변형 관련 상수 (자바 상수 활용)
        private const val VARIANT_CLEAR_MASK = Long.MAX_VALUE // 최상위 비트 제외 모든 비트 1
        private const val VARIANT_BITS = Long.MIN_VALUE       // 최상위 비트만 1
    }
    private data class CacheKey(val workspace: UUID, val types: List<TypeKey>)
    private data class TypeKey(val id: String, val version: String)
}
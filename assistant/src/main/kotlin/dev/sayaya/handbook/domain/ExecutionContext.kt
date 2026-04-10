package dev.sayaya.handbook.domain

import reactor.core.Disposable
import reactor.core.publisher.Sinks
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * 개별 실행의 런타임 상태를 보유하는 컨텍스트.
 *
 * <p><b>책임:</b> 실행 중인 계획의 Disposable, 응답 대기 Sink, 진행률 등
 * 실행 라이프사이클에 필요한 모든 가변 상태를 관리한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 데이터 홀더)</p>
 *
 * <p><b>주의:</b> ConcurrentHashMap에 저장되므로 필드 접근은 Atomic 타입을 통해 스레드 안전하게 수행된다.</p>
 *
 * @param id 실행 고유 식별자
 * @param auditId 연결된 감사 기록 ID
 * @param workspace 워크스페이스 ID
 * @param plan 실행 중인 계획
 * @param disposable 실행 스트림의 Disposable (취소 시 사용)
 * @param responseSink AWAIT_CONFIRM 시 사용자 응답을 받는 Sink
 * @param currentGroup 현재 실행 중인 그룹 번호
 * @param totalGroups 전체 그룹 수
 * @param status 실행 상태 (PENDING, EXECUTING, COMPLETED, ABORTED)
 */
data class ExecutionContext(
    val id: UUID = UUID.randomUUID(),
    val auditId: UUID,
    val workspace: UUID,
    val plan: ExecutionPlan,
    val disposable: AtomicReference<Disposable?> = AtomicReference(null),
    val responseSink: AtomicReference<Sinks.One<String>?> = AtomicReference(null),
    val currentGroup: AtomicInteger = AtomicInteger(0),
    val totalGroups: Int = 0,
    val status: AtomicReference<String> = AtomicReference("PENDING"),
)

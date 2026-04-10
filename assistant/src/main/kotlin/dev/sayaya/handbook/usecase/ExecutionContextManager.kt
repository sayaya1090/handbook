package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.ExecutionContext
import dev.sayaya.handbook.domain.ExecutionPlan
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 활성 실행 컨텍스트의 생명주기를 관리하는 매니저.
 *
 * <p><b>책임:</b> ConcurrentHashMap에 ExecutionContext를 등록/조회/제거하며,
 * 워크스페이스별 활성 실행 목록 조회, AWAIT_CONFIRM 응답 전달,
 * 부모-자식 실행 간 관계 관리를 담당한다.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 인메모리 상태 관리)</p>
 *
 * <p><b>주의:</b> ConcurrentHashMap과 ExecutionContext 내부의 Atomic 필드를 통해
 * 스레드 안전성이 보장된다. 실행 완료/에러/취소 시 반드시 [remove]를 호출하여 메모리 누수를 방지해야 한다.
 * 부모 실행 제거 시 자식 매핑도 함께 정리된다.</p>
 */
class ExecutionContextManager {
    private val executions: ConcurrentHashMap<UUID, ExecutionContext> = ConcurrentHashMap()
    private val parentChildren: ConcurrentHashMap<UUID, MutableList<UUID>> = ConcurrentHashMap()

    /**
     * 새 실행 컨텍스트를 등록한다.
     */
    fun register(executionId: UUID, workspace: UUID, plan: ExecutionPlan): ExecutionContext {
        val totalGroups = plan.steps.groupBy { it.group }.size
        val context = ExecutionContext(
            id = executionId,
            auditId = executionId,
            workspace = workspace,
            plan = plan,
            totalGroups = totalGroups,
        )
        executions[executionId] = context
        return context
    }

    /**
     * 실행 ID로 컨텍스트를 조회한다. 없으면 null.
     */
    fun get(executionId: UUID): ExecutionContext? = executions[executionId]

    /**
     * 실행 ID로 컨텍스트를 제거하고 반환한다. 없으면 null.
     */
    fun remove(executionId: UUID): ExecutionContext? = executions.remove(executionId)

    /**
     * 워크스페이스의 활성 실행 목록을 반환한다.
     */
    fun listByWorkspace(workspace: UUID): List<ExecutionContext> =
        executions.values.filter { it.workspace == workspace }

    /**
     * AWAIT_CONFIRM에 대한 사용자 응답을 특정 실행에 전달한다.
     * 대기 중인 responseSink에 응답을 emit하여 실행 스트림을 재개한다.
     */
    fun respondTo(executionId: UUID, response: String) {
        val context = executions[executionId]
        if (context != null) {
            val sink = context.responseSink.getAndSet(null)
            sink?.tryEmitValue(response)
        }
    }

    /**
     * 자식 실행을 부모에 등록하고, 자식 컨텍스트를 생성하여 반환한다.
     */
    fun registerChild(parentId: UUID, childId: UUID, workspace: UUID, plan: ExecutionPlan): ExecutionContext {
        val child = register(childId, workspace, plan)
        parentChildren.computeIfAbsent(parentId) { mutableListOf() }.add(childId)
        return child
    }

    /**
     * 부모 ID에 연결된 자식 실행 컨텍스트 목록을 반환한다.
     */
    fun listChildren(parentId: UUID): List<ExecutionContext> {
        val childIds = parentChildren[parentId] ?: return emptyList()
        return childIds.mapNotNull { executions[it] }
    }
}

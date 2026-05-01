package dev.sayaya.handbook.interfaces.schedule

import java.util.*

/**
 * 활성 워크스페이스 목록 제공 포트.
 *
 * **책임:** 현재 시스템에서 활성 상태인 워크스페이스의 ID 목록을 반환한다.
 * 스케줄 기반 품질 감시에서 스캔 대상을 결정하는 데 사용된다.
 */
interface WorkspaceProvider {
    fun getActiveWorkspaces(): List<UUID>
}

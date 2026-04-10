package dev.sayaya.handbook.test

import com.microsoft.playwright.Page

/**
 * GWT 테스트용 이벤트 발행 유틸리티.
 *
 * workspace-event(SSE), mutate(에이전트), agent-command 등
 * CustomEvent 발행을 간결하게 지원한다.
 */
object Events {

    /** SSE 워크스페이스 이벤트를 발행한다. (TYPE_CREATED, DOCUMENT_DELETED, PRESENCE 등) */
    fun Page.dispatchWorkspaceEvent(eventType: String, payload: String) {
        evaluate("window.dispatchEvent(new CustomEvent('handbook-workspace-event',{detail:'$eventType:$payload',bubbles:false}))")
    }

    /** 에이전트 mutate 이벤트를 발행한다. (CREATE, DELETE, SET, ADD 등) */
    fun Page.dispatchMutateEvent(vararg commands: String) {
        val detail = commands.joinToString(",") { "'$it'" }
        evaluate("window.dispatchEvent(new CustomEvent('handbook-mutate',{detail:[$detail],bubbles:false}))")
    }

    /** AGENT_COMMAND SSE 이벤트를 발행한다. */
    fun Page.dispatchAgentCommand(type: String, payload: String) {
        dispatchWorkspaceEvent("AGENT_COMMAND", """{"type":"$type","payload":$payload}""")
    }

    /** PRESENCE 이벤트를 발행한다. */
    fun Page.dispatchPresence(user: String, type: String? = null, serial: String? = null, field: String? = null) {
        val json = buildString {
            append("""{"user":"$user","userName":"$user"""")
            append(""","type":${type?.let { "\"$it\"" } ?: "null"}""")
            append(""","serial":${serial?.let { "\"$it\"" } ?: "null"}""")
            append(""","field":${field?.let { "\"$it\"" } ?: "null"}""")
            append("}")
        }
        dispatchWorkspaceEvent("PRESENCE", json)
    }
}

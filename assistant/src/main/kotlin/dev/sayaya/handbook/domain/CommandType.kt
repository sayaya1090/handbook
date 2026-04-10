package dev.sayaya.handbook.domain

/**
 * 에이전트가 프론트엔드에 전송하는 UI 커맨드 타입.
 * agent-protocol 모듈의 CommandType과 1:1 대응한다.
 */
enum class CommandType {
    NAVIGATE,
    HIGHLIGHT,
    ATTENTION,
    SCROLL,
    PREVIEW,
    MUTATE,
    NOTIFY,
    PROGRESS,
    AWAIT_CONFIRM,
    COMPLETE,
    DELEGATE,
}

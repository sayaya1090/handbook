package dev.sayaya.handbook.domain

import java.io.Serializable

/**
 * 두 버전 간 변경점을 나타내는 결과 DTO.
 *
 * **책임:** 속성/필드의 추가, 삭제, 변경을 "before → after" 형태로 표현한다.
 * 프론트엔드의 DiffPanel에서 직접 렌더링할 수 있는 구조.
 *
 * @property changes 변경 항목 리스트 ("필드명: before → after" 형식)
 * @property added 추가된 항목 이름 리스트
 * @property removed 삭제된 항목 이름 리스트
 */
data class DiffResult(
    val changes: List<String> = emptyList(),
    val added: List<String> = emptyList(),
    val removed: List<String> = emptyList(),
) : Serializable

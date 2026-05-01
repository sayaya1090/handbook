package dev.sayaya.handbook.interfaces.event

import dev.sayaya.handbook.usecase.QualityMonitorService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

/**
 * UC-A8: VALIDATION_REQUESTED Kafka 이벤트 트리거 검증 테스트.
 *
 * ValidationEventListener가 Kafka 이벤트 JSON을 올바르게 파싱하여
 * QualityMonitorService.validate()를 호출하는지 검증한다.
 */
class ValidationEventListenerTest : BehaviorSpec({
    val qualityMonitorService = mockk<QualityMonitorService>()
    val objectMapper: ObjectMapper = jacksonObjectMapper()
    val listener = ValidationEventListener(qualityMonitorService, objectMapper)

    Given("VALIDATION_REQUESTED 이벤트가 수신되었을 때") {
        val workspace = UUID.randomUUID()
        val typeId = "customer"
        val typeVersion = "v2"
        val documentId = "DOC-001"

        every {
            qualityMonitorService.validate(workspace, typeId, typeVersion, documentId)
        } returns Mono.empty()

        When("전체 필드가 포함된 이벤트를 처리하면") {
            val event = objectMapper.writeValueAsString(mapOf(
                "event_type" to "VALIDATION_REQUESTED",
                "workspace" to workspace.toString(),
                "payload" to mapOf(
                    "type_id" to typeId,
                    "type_version" to typeVersion,
                    "document_id" to documentId,
                ),
            ))
            listener.accept(event)

            Then("QualityMonitorService.validate가 올바른 인자로 호출된다") {
                verify(exactly = 1) {
                    qualityMonitorService.validate(workspace, typeId, typeVersion, documentId)
                }
            }
        }
    }

    Given("typeVersion과 documentId가 없는 VALIDATION_REQUESTED 이벤트가 수신되었을 때") {
        val workspace = UUID.randomUUID()
        val typeId = "order"

        every {
            qualityMonitorService.validate(workspace, typeId, null, null)
        } returns Mono.empty()

        When("선택적 필드 없이 이벤트를 처리하면") {
            val event = objectMapper.writeValueAsString(mapOf(
                "event_type" to "VALIDATION_REQUESTED",
                "workspace" to workspace.toString(),
                "payload" to mapOf(
                    "type_id" to typeId,
                ),
            ))
            listener.accept(event)

            Then("typeVersion과 documentId가 null로 전달된다") {
                verify(exactly = 1) {
                    qualityMonitorService.validate(workspace, typeId, null, null)
                }
            }
        }
    }

    Given("VALIDATION_REQUESTED가 아닌 이벤트가 수신되었을 때") {
        When("DOCUMENT_CREATED 이벤트를 처리하면") {
            val event = objectMapper.writeValueAsString(mapOf(
                "event_type" to "DOCUMENT_CREATED",
                "workspace" to UUID.randomUUID().toString(),
                "payload" to mapOf("type_id" to "test"),
            ))
            listener.accept(event)

            Then("QualityMonitorService.validate가 호출되지 않는다") {
                verify(exactly = 0) {
                    qualityMonitorService.validate(any(), eq("test"), any(), any())
                }
            }
        }
    }

    Given("event_type 필드가 없는 이벤트가 수신되었을 때") {
        When("이벤트를 처리하면") {
            val event = objectMapper.writeValueAsString(mapOf(
                "workspace" to UUID.randomUUID().toString(),
                "payload" to mapOf("type_id" to "test"),
            ))
            listener.accept(event)

            Then("QualityMonitorService.validate가 호출되지 않는다 (무시)") {
                verify(exactly = 0) {
                    qualityMonitorService.validate(any(), eq("test"), any(), any())
                }
            }
        }
    }

    Given("payload가 없는 VALIDATION_REQUESTED 이벤트가 수신되었을 때") {
        When("이벤트를 처리하면") {
            val event = objectMapper.writeValueAsString(mapOf(
                "event_type" to "VALIDATION_REQUESTED",
                "workspace" to UUID.randomUUID().toString(),
            ))
            listener.accept(event)

            Then("예외 없이 무시된다") {
                // payload가 null이면 early return하므로 validate가 호출되지 않음
            }
        }
    }

    Given("잘못된 JSON 형식의 이벤트가 수신되었을 때") {
        When("이벤트를 처리하면") {
            listener.accept("invalid json {{{")

            Then("예외가 로그되고 정상적으로 종료된다") {
                // Exception이 catch되어 로그만 남기므로 테스트가 통과함
            }
        }
    }
})

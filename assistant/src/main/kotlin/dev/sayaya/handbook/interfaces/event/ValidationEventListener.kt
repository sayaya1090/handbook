package dev.sayaya.handbook.interfaces.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.sayaya.handbook.domain.event.Event
import dev.sayaya.handbook.domain.event.ValidationPayload
import dev.sayaya.handbook.usecase.QualityMonitorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.function.Consumer

/**
 * VALIDATION_REQUESTED Kafka 이벤트를 소비하여 문서 검증을 트리거한다.
 *
 * **책임:** handbook-events 토픽에서 VALIDATION_REQUESTED 이벤트만 필터링하여
 * [QualityMonitorService]에 검증을 위임한다. 다른 이벤트 타입은 무시한다.
 *
 * **의존관계:**
 * - [QualityMonitorService] — 실제 검증 로직 실행
 * - [ObjectMapper] — 이벤트 JSON 역직렬화
 *
 * **주의:** Spring Cloud Stream의 Consumer 바인딩으로 연결된다.
 * Bean 이름 "validation"이 bindings 설정의 destination과 매핑된다.
 */
@Component("validation")
class ValidationEventListener(
    private val qualityMonitorService: QualityMonitorService,
    private val objectMapper: ObjectMapper,
) : Consumer<String> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun accept(event: String) {
        try {
            val map: Map<String, Any> = objectMapper.readValue(event)
            val eventType = map["event_type"]?.toString() ?: return
            if (eventType != "VALIDATION_REQUESTED") return

            val workspace = UUID.fromString(map["workspace"]?.toString() ?: return)
            val payloadMap = map["payload"] as? Map<*, *> ?: return
            val typeId = payloadMap["type_id"]?.toString() ?: return
            val typeVersion = payloadMap["type_version"]?.toString()
            val documentId = payloadMap["document_id"]?.toString()

            logger.info("Validation requested: workspace={}, typeId={}, typeVersion={}, documentId={}", workspace, typeId, typeVersion, documentId)
            qualityMonitorService.validate(workspace, typeId, typeVersion, documentId).subscribe()
        } catch (e: Exception) {
            logger.error("Failed to process validation event: {}", e.message, e)
        }
    }
}

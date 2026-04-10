package dev.sayaya.handbook.interfaces.quality

import dev.sayaya.handbook.domain.QualityIssue
import dev.sayaya.handbook.domain.QualityIssue.Severity
import dev.sayaya.handbook.usecase.QualityMonitor
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sqrt

class DefaultQualityMonitor(
    private val searchDocumentClient: WebClient,
) : QualityMonitor {

    override fun scan(workspace: UUID): Flux<QualityIssue> {
        return fetchDocuments(workspace).collectList().flatMapMany { documents ->
            val missingFieldIssues = checkMissingRequiredFields(documents)
            val duplicateIssues = checkDuplicateSerials(documents)
            val anomalyIssues = checkNumericAnomalies(documents)
            Flux.fromIterable(missingFieldIssues + duplicateIssues + anomalyIssues)
        }
    }

    private fun fetchDocuments(workspace: UUID): Flux<DocumentSnapshot> {
        return searchDocumentClient.get()
            .uri("/document?workspace={workspace}&size=10000", workspace)
            .retrieve()
            .bodyToFlux(DocumentSnapshot::class.java)
    }

    private fun checkMissingRequiredFields(documents: List<DocumentSnapshot>): List<QualityIssue> {
        return documents.flatMap { doc ->
            doc.requiredFields.orEmpty()
                .filter { field -> doc.data[field] == null }
                .map { field ->
                    QualityIssue(
                        type = doc.type,
                        serial = doc.serial,
                        field = field,
                        severity = Severity.ERROR,
                        message = "필수 필드 '$field'에 값이 없습니다",
                    )
                }
        }
    }

    private fun checkDuplicateSerials(documents: List<DocumentSnapshot>): List<QualityIssue> {
        return documents.groupBy { it.type to it.serial }
            .filter { it.value.size > 1 }
            .flatMap { (key, duplicates) ->
                duplicates.map { doc ->
                    QualityIssue(
                        type = doc.type,
                        serial = doc.serial,
                        field = null,
                        severity = Severity.WARNING,
                        message = "타입 '${key.first}'에서 시리얼 '${key.second}'이 ${duplicates.size}건 중복됩니다",
                    )
                }
            }
    }

    private fun checkNumericAnomalies(documents: List<DocumentSnapshot>): List<QualityIssue> {
        val numericFields = documents.flatMap { it.data.keys }.distinct()
        val issues = mutableListOf<QualityIssue>()
        for (field in numericFields) {
            val values = documents.mapNotNull { doc -> doc.data[field]?.toDoubleOrNull()?.let { doc to it } }
            if (values.size < 3) continue
            val nums = values.map { it.second }
            val mean = nums.average()
            val stdDev = sqrt(nums.map { (it - mean) * (it - mean) }.average())
            if (stdDev == 0.0) continue
            values.filter { abs(it.second - mean) > 3 * stdDev }.forEach { (doc, value) ->
                issues.add(QualityIssue(
                    type = doc.type,
                    serial = doc.serial,
                    field = field,
                    severity = Severity.WARNING,
                    message = "필드 '$field' 값 ${value}이(가) 평균에서 3σ 이상 벗어났습니다 (평균: ${"%.2f".format(mean)}, 표준편차: ${"%.2f".format(stdDev)})",
                ))
            }
        }
        return issues
    }

    data class DocumentSnapshot(
        val type: String,
        val serial: String,
        val data: Map<String, String?>,
        val requiredFields: List<String>?,
    )
}

package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document

/**
 * 문서 리스트를 CSV 문자열로 직렬화하는 유틸리티.
 *
 * **책임:** Document 리스트에서 고정 컬럼(type, serial, effectDateTime, expireDateTime, status)과
 * data 맵의 동적 컬럼을 추출하여 RFC 4180 호환 CSV를 생성한다.
 *
 * **의존관계:** 없음 (순수 유틸리티)
 *
 * **주의:** data 맵의 키 집합이 문서마다 다를 수 있으므로, 전체 문서에서 키를 수집한 후 통합 헤더를 생성한다.
 * 값에 쉼표/줄바꿈/따옴표가 포함된 경우 RFC 4180 규칙에 따라 이스케이프한다.
 */
object CsvSerializer {
    private val FIXED_COLUMNS = listOf("type", "serial", "effect_date_time", "expire_date_time", "status")

    /**
     * 문서 리스트를 CSV 문자열로 변환한다.
     * @param documents 직렬화할 문서 리스트
     * @return CSV 문자열 (헤더 행 포함, 문서가 비어있으면 헤더만 반환)
     */
    fun serialize(documents: List<Document>): String {
        val dataKeys = documents.flatMap { it.data.keys }.distinct().sorted()
        val allColumns = FIXED_COLUMNS + dataKeys

        val sb = StringBuilder()
        // Header row
        sb.appendLine(allColumns.joinToString(",") { escapeCsv(it) })

        // Data rows
        for (doc in documents) {
            val fixedValues = listOf(
                doc.type,
                doc.serial,
                doc.effectDateTime.toString(),
                doc.expireDateTime.toString(),
                doc.status,
            )
            val dataValues = dataKeys.map { key -> doc.data[key] ?: "" }
            val allValues = fixedValues + dataValues
            sb.appendLine(allValues.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}

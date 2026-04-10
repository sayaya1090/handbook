package dev.sayaya.handbook.domain

data class QualityIssue(
    val type: String,
    val serial: String,
    val field: String?,
    val severity: Severity,
    val message: String,
) {
    enum class Severity { INFO, WARNING, ERROR }
}

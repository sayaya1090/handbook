package dev.sayaya.handbook.entity.validator

data class ValidatorDate (
    val format: String? = "yyyy-MM-dd", // 날짜 포맷
    val before: String? = null, // 이 날짜 이전이어야 함 (ISO 8601 형식의 날짜 문자열)
    val after: String? = null   // 이 날짜 이후여야 함 (ISO 8601 형식의 날짜 문자열)
) : ValidatorDefinition {
    override val type: ValidatorType get() = ValidatorType.DATE
}

package dev.sayaya.handbook.entity.validator

import java.time.Instant

data class ValidatorDate (
    val before: Instant? = null, // 이 날짜 이전이어야 함
    val after: Instant? = null   // 이 날짜 이후여야 함
) : ValidatorDefinition {
    override val type: ValidatorType get() = ValidatorType.DATE
}

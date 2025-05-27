package dev.sayaya.handbook.entity.validator

data class ValidatorNumber (
    val min: Number? = null,
    val max: Number? = null,
    val format: String? = null // 숫자 포맷 (예: "#,##0.00")
    // 예: val integerOnly: Boolean? = false
) : ValidatorDefinition {
    override val type: ValidatorType get() = ValidatorType.NUMBER
}

package dev.sayaya.handbook.entity.validator

data class ValidatorNumber (
    val min: Number? = null,
    val max: Number? = null
) : ValidatorDefinition {
    override val type: ValidatorType get() = ValidatorType.NUMBER
}

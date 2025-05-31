package dev.sayaya.handbook.entity.validator

data class ValidatorEnum (
    val allowedValues: List<String>
) : ValidatorDefinition {
    constructor(vararg allowedValues: String): this(allowedValues.toList())
    override val type: ValidatorType get() = ValidatorType.ENUM
}

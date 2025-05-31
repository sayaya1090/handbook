package dev.sayaya.handbook.entity.validator

data class ValidatorRegex (
    val pattern: String
) : ValidatorDefinition {
    override val type: ValidatorType = ValidatorType.REGEX
}

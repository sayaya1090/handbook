package dev.sayaya.handbook.entity.validator

data class ValidatorBool (
    override val type: ValidatorType = ValidatorType.BOOL
) : ValidatorDefinition

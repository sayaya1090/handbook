package dev.sayaya.handbook.domain

enum class AttributeType {
    Value,  // File, Document가 아닌 1개 값(텍스트, 날짜, 숫자, ...)
    Array,  // x개 값
    Map,    // Key-Value 형태
    File,
    Document
}
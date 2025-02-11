package dev.sayaya.handbook.entity

enum class AttributeType {
    Value,  // 범위가 없는 1개 값
    Array,  // 범위가 없는 x개 값
    Enum,   // 정해진 범위 중 1개의 값
    EnumSet,// 정해진 범위 중 x개의 값
    Map,    // Key-Value 형태
    File,
    Document
}
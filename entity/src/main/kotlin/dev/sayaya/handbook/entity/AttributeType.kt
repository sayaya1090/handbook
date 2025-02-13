package dev.sayaya.handbook.entity

/*
  Validator와 조합하여 다양한 값을 표현한다.
  단순값: Value + Validator를 사용한 타입체크
  N개 중 하나: Value + Enum validator
  N개 중 여러개: Array { Value + Enum validator }
  테이블: Map { Value + Enum validator // 키 }, { Value + Regex|Type Validator // 값 }
  2차원 테이블: Map { Value + Enum validator // 키1 }, { Map { Value + Enum validator // 키2 }, { Value + Regex|Type Validator // 값 } }
  특정 타입의 문서 참조: Document + Type Validator
  이미지: File + File Validator
 */
enum class AttributeType {
    Value,  // File, Document가 아닌 1개 값(텍스트, 날짜, 숫자, ...)
    Array,  // x개 값
    Map,    // Key-Value 형태
    File,
    Document
}
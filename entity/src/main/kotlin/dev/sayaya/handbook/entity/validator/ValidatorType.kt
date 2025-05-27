package dev.sayaya.handbook.entity.validator

enum class ValidatorType {
    REGEX,          // 값이 정규표현식을 만족한다
    BOOL, NUMBER, DATE,   // 값이 해당 타입이다(포맷, 범위 지원)
    ENUM,           // 값이 해당 값 중 하나이다. 파일 속성의 경우, 확장자가 해당 값 중 하나이다
}
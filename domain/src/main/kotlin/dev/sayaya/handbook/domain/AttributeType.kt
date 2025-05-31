package dev.sayaya.handbook.domain

enum class AttributeType {
    Value,  /* File, Document가 아닌 1개 값(텍스트, 날짜, 숫자, ...), 해당 값에 Validator가 적용됨. Validator 종류에 따라 입력 컴포넌트 변경
               Regex: 일반 문자열. Regex는 여러개 추가할 수 있음
               Bool: 체크박스. 다른 Validator와 함께할 수 없음
               Number: 숫자. Min, Max 정의. 다른 Validator와 함께할 수 없음
               Date: 날짜. ISO 문자열로 값 저장. after, before 정의. 다른 Validator와 함께할 수 없음
               Enum: 셀렉트박스. 다른 Validator와 함께할 수 없음 */
    Array,  // x개 값, Validator는 타입에 위임되어 각각 적용됨
    Map,    // Key-Value 형태, Validator는 각 Key와 Value에 위임되어 각각 서로 다른 Validator가 적용됨
    File,   // Validator를 따로 정의하지 않음. extensions에 따라 확장자를 검사
    Document // Validator를 따로 정의하지 않음. referencedType에 따라 타입 검사
}
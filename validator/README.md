## 검증 모듈
타입이나 도큐먼트 변경 이벤트가 발생하면, 연관 도큐먼트를 검증하고 결과를 업로드
타입 조회를 효율적으로 하기 위해 레디스 캐시 사용, 타입 변경 시 캐시 업데이트
카페인X, 레디스 이유: 타입 변경 이벤트는 밸리데이터 컨슈머 그룹에 의해 하나의 인스턴스에서 처리됨. 멀티 인스턴스간의 캐시 공유


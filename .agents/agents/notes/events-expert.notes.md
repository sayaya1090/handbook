## 요청 로그

- 2026-04-18: Kafka 직렬화 수정 -> workspace-command Kafka 500 에러 해결을 위해 StringSerializer를 ByteArraySerializer로 교체

---

# events-expert Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

(미확보)

## 반복 함정

(미확보)

## 내부 체크리스트

- [ ] 새 이벤트 타입 추가 시 -> Avro 스키마 정의 및 모듈 배포 확인

## 과거 실수

(미확보)

## 원칙 갱신 제안

- **이벤트 명명 규칙 (2026-05-03)**: 워크스페이스 컨텍스트 공유는 `handbook-workspace-context`, 일반 도메인 이벤트 브릿징(SSE 수신 등)은 `handbook-workspace-event`를 사용한다.

## 아카이브 요약

(없음)

---

마지막 감사: — (신규)

## 요청 로그

- 2026-05-14: 클린 URL 전환 감사 -> 용어 일관성 확인, Gateway Fallback 괴리 발견
- 2026-05-12: ES 전환 문서 감사 -> 아키텍처/스키마 및 노트 정합성 업데이트
- 2026-04-23: Global Kover 제외 설정 -> Application, Config, Properties 클래스 제외로 테스트 품질 지표 개선

---

# docs-keeper Operational Notes

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).

## 탐색 패턴

- **클린 URL 전환 시 크로스체크**: `docs/requirements.md` (정책) ↔ `docs/contracts/menus.md` (매칭 규약) ↔ `shell-ui/README.md` (구현 상세) ↔ `gateway/application.yml` (Fallback 라우팅) 4개 지점의 일관성을 확인해야 함.

## 반복 함정

(미확보)

## 내부 체크리스트

- [ ] 아키텍처 변경 시 -> docs/architecture.md 다이어그램 업데이트 확인
- [ ] 요구사항 변경 시 -> docs/requirements.md 및 관련 유스케이스 문서 일관성 확인
- [ ] 클린 URL 도입 시 -> Gateway Fallback 설정 누락 여부 확인

## 과거 실수

(미확보)

## 원칙 갱신 제안

- **Gateway Fallback 자동 동기화**: `MenuSupplier`들이 제공하는 `urlRegex` 경로들이 Gateway의 Fallback(`app.html`) 라우트에 자동으로 반영되는지 검증하는 테스트나 프로세스 제안 필요.

## 아카이브 요약

(없음)

---

마지막 감사: 2026-05-14 (클린 URL 전환 및 URI 정규화 작업 일관성 검토)

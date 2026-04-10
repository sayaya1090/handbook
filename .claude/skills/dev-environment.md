# 개발 환경 설정

## 인프라
```bash
docker-compose up -d  # PostgreSQL + Kafka
```

## 환경변수 (기본값 사용 가능)
| 변수 | 기본값 | 설명 |
|------|--------|------|
| DB_HOST | localhost | PostgreSQL 호스트 |
| DB_PORT | 5432 | PostgreSQL 포트 |
| DB_NAME | handbook | 데이터베이스 이름 |
| DB_USER | handbook | DB 사용자 |
| DB_PASSWORD | handbook | DB 비밀번호 |
| KAFKA_BROKERS | localhost:9092 | Kafka 브로커 |
| JWT_SECRET | default-dev-secret... | JWT 서명 키 (PEM) |
| GOOGLE_CLIENT_ID | - | OAuth2 클라이언트 ID |
| GOOGLE_CLIENT_SECRET | - | OAuth2 클라이언트 시크릿 |

## 서비스 포트
| 서비스 | 포트 | 설명 |
|--------|------|------|
| gateway | 8080 | API 게이트웨이 |
| login | 8081 | OAuth2 인증 |
| search-type | 8082 | 타입 조회 (CQRS) |
| persist-type | 8083 | 타입 저장 |
| search-document | 8084 | 문서 조회 (CQRS) |
| persist-document | 8085 | 문서 저장 |
| persist-workspace | 8086 | 워크스페이스 관리 |
| assistant | 8087 | AI 에이전트 |
| event-broadcaster | 8088 | Kafka → SSE |

## 빌드 & 테스트

### 전체 테스트
```bash
./gradlew test                    # 백엔드 + GWT Playwright 테스트 (E2E 제외)
E2E=true ./gradlew :e2e:test      # E2E (서버 실행 필요)
```

### 모듈별
```bash
./gradlew :모듈:compileJava       # 컴파일
./gradlew :모듈:compileKotlin     # Kotlin 컴파일
./gradlew :모듈:test              # 테스트
./gradlew :모듈:gwtDev            # GWT DevMode
./gradlew :gateway:bootRun        # 백엔드 실행
```

### 주의사항
- 통합 테스트(R2dbc*IntegrationTest)는 Testcontainers로 PostgreSQL 자동 실행
- GWT 테스트는 모듈별 고유 포트 할당 (병렬 실행 가능, `--parallel`)
- E2E 테스트는 게이트웨이 + 모든 백엔드 서비스 실행 필요

## Prometheus / Grafana 로컬 설정 (7.4 관측성)

### docker-compose 추가 서비스

```yaml
# docker-compose.yml에 추가
prometheus:
  image: prom/prometheus:latest
  ports:
    - "9090:9090"
  volumes:
    - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana:latest
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin
```

### prometheus.yml 예시

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
  - job_name: 'persist-document'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8085']
  - job_name: 'search-document'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8084']
  - job_name: 'event-broadcaster'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8088']
  - job_name: 'assistant'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8087']
```

### Spring Boot Actuator 설정 (각 서비스)

```yaml
# application.yml에 추가
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,metrics
  metrics:
    tags:
      application: ${spring.application.name}
```

### 주요 모니터링 메트릭

| 메트릭 | 설명 |
|--------|------|
| `http_server_requests_seconds` | HTTP 요청 지연 시간 (히스토그램) |
| `r2dbc_pool_acquired` | R2DBC 풀 사용 중 커넥션 수 |
| `r2dbc_pool_pending` | R2DBC 풀 대기 중 요청 수 |
| `kafka_consumer_records_lag` | Kafka 컨슈머 랙 |
| `dlq_events_total` | DLQ에 저장된 이벤트 수 (커스텀) |

### 접속 URL
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

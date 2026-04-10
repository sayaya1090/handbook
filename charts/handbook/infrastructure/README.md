# Handbook Infrastructure Chart

Handbook 프로젝트의 인프라스트럭처 컴포넌트를 관리하는 Helm 차트입니다.

## 구성 요소

- **CloudNativePG**: PostgreSQL 클러스터 (3 인스턴스, HA 구성)
- **S3 Bucket**: Ceph 기반 오브젝트 스토리지

## 설정

### Values 구조

```yaml
database:
  ip: ''                    # LoadBalancer IP
  persistence:
    size: 10Gi             # 스토리지 크기
    storageClass: ''       # 스토리지 클래스
  backup:
    schedule: '0 3 * * *'  # Cron 형식 백업 스케줄 (비어있으면 백업 안 함)
  bootstrap:
    restore: false         # 초기 복구 모드 활성화
    restorePath: ''        # S3 복구 경로 (예: 'old', 'backup/2024-01-01')

s3:
  bucket:
    name: ''               # 버킷 이름
    maxSize: 10G           # 버킷 최대 크기
```

### 스테이지별 기본 설정

| Stage | 백업 주기 | 버킷 크기 |
|-------|----------|----------|
| dev | 매주 일요일 03:00 | 10G |
| staging | 백업 안 함 | 20G |
| prod | 매일 03:00 | 50G |

## 데이터베이스 백업 및 복구

### 백업 설정

백업은 CloudNativePG의 `ScheduledBackup`을 통해 자동으로 수행됩니다.

```yaml
database:
  backup:
    schedule: '0 3 * * *'  # 매일 새벽 3시
```

스케줄을 비워두면 백업이 비활성화됩니다:
```yaml
database:
  backup:
    schedule: ''  # 백업 안 함
```

### 수동 백업

즉시 백업을 생성하려면:

```bash
kubectl create -f - <<EOF
apiVersion: postgresql.cnpg.io/v1
kind: Backup
metadata:
  name: postgresql-backup-$(date +%Y%m%d-%H%M%S)
  namespace: handbook-prod
spec:
  cluster:
    name: postgresql
EOF
```

### 백업 확인

```bash
# 백업 목록 조회
kubectl get backups -n handbook-prod

# 백업 상세 정보
kubectl describe backup <backup-name> -n handbook-prod
```

### 복구 방법

#### 1. 새 클러스터로 복구

기존 백업에서 새로운 PostgreSQL 클러스터를 생성합니다.

##### 최신 백업으로 복구 (기본)

`restorePath`를 비워두면 **가장 최신 백업이 자동으로 선택**됩니다:

```yaml
database:
  bootstrap:
    restore: true
    restorePath: ''  # 가장 최신 백업 자동 선택
```

##### 특정 경로의 백업에서 복구

```yaml
database:
  bootstrap:
    restore: true
    restorePath: 'old'  # s3://<bucket>/old 경로의 가장 최신 백업 사용
```

##### 특정 백업 ID로 복구

```yaml
database:
  bootstrap:
    restore: true
    restorePath: ''
    recoveryTarget:
      backupID: '20240115T030000'  # 특정 백업 ID 지정
```

#### 2. 복구 절차

1. **기존 클러스터 백업 (선택사항)**
   ```bash
   # 혹시 모를 상황에 대비해 현재 데이터 백업
   kubectl create -f - <<EOF
   apiVersion: postgresql.cnpg.io/v1
   kind: Backup
   metadata:
     name: postgresql-before-restore
     namespace: handbook-prod
   spec:
     cluster:
       name: postgresql
   EOF
   ```

2. **클러스터 삭제**
   ```bash
   kubectl delete cluster postgresql -n handbook-prod
   ```

3. **복구 설정 적용**

   `values.yaml` 수정:
   ```yaml
   database:
     bootstrap:
       restore: true
       restorePath: 'backup/2024-01-01'  # 복구할 백업 경로
   ```

4. **차트 재배포**
   ```bash
   helm upgrade handbook-infra-prod . \
     --namespace handbook-prod \
     --values values.yaml
   ```

5. **복구 완료 후 설정 원복**

   복구가 완료되면 `bootstrap.restore`를 `false`로 변경:
   ```yaml
   database:
     bootstrap:
       restore: false
       restorePath: ''
   ```

#### 3. PITR (Point-in-Time Recovery)

특정 시점으로 복구 (WAL 아카이브를 사용하여 정확한 시점 복구):

```yaml
database:
  bootstrap:
    restore: true
    restorePath: ''
    recoveryTarget:
      targetTime: "2024-01-15 10:30:00"  # UTC 기준, 가장 가까운 백업에서 이 시점까지 WAL 적용
```

> **참고**: CloudNativePG는 지정된 경로에서 **가장 최신 백업을 자동으로 선택**합니다.
> - `restorePath`가 비어있으면: 버킷 루트의 최신 백업 사용
> - `restorePath`가 지정되면: 해당 경로의 최신 백업 사용
> - 특정 백업을 원하면 `recoveryTarget.backupID` 사용

### 주의사항

- `bootstrap.restore: true`로 설정하면 기존 데이터가 모두 삭제되고 백업에서 복구됩니다
- 복구 작업 전 반드시 현재 데이터 백업을 권장합니다
- 복구 완료 후 `restore: false`로 변경하지 않으면 다음 재배포 시 다시 복구가 실행됩니다

## 데이터베이스 접속

### 클러스터 내부 접속

```bash
# Read-Write 접속
kubectl get secret postgres -n handbook-prod -o jsonpath='{.data.password}' | base64 -d
psql -h postgresql-rw.handbook-prod.svc -U postgres -d handbook

# Read-Only 접속
psql -h postgresql-ro.handbook-prod.svc -U postgres -d handbook
```

### 외부 접속

LoadBalancer를 통한 접속:

```bash
psql -h <database.ip> -U postgres -d handbook
```

## 트러블슈팅

### 백업 실패

```bash
# 백업 로그 확인
kubectl logs -n handbook-prod -l cnpg.io/cluster=postgresql

# S3 접속 정보 확인
kubectl get secret bucket -n handbook-prod -o yaml
```

### 클러스터 상태 확인

```bash
# 클러스터 상태
kubectl get cluster postgresql -n handbook-prod

# 인스턴스 상태
kubectl get pods -n handbook-prod -l cnpg.io/cluster=postgresql

# 상세 정보
kubectl describe cluster postgresql -n handbook-prod
```

### 동기화 복제 확인

```bash
kubectl exec -it postgresql-1 -n handbook-prod -- \
  psql -U postgres -c "SELECT * FROM pg_stat_replication;"
```

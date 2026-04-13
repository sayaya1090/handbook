# handbook 차트

## 사전 준비: JWT 시크릿

각 서비스 Deployment는 `JWT_SECRET` 환경변수를 `handbook-jwt` 시크릿에서 읽어 컨테이너에 주입한다. 해당 시크릿은 **Git에 커밋하지 않는다** — 클러스터에 직접 생성한다.

### 1) RSA 키 쌍 생성

```bash
openssl genpkey -algorithm RSA -out jwt-private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
```

`login` 서비스는 서명을 위해 개인키가 필요하고, 나머지 서비스는 검증용 공개키만 있으면 된다. `authentication` 모듈의 `Pem` 클래스는 개인키가 주어져도 공개키를 추출하므로 운영 편의상 하나의 시크릿에 개인키만 넣어 전 서비스에 공유해도 동작한다.

### 2) OpenShift/Kubernetes Secret 생성

```bash
# 네임스페이스
NS=handbook

# 전 서비스 공용 (개인키 한 장을 공유하는 단순 구성)
oc create secret generic handbook-jwt \
  --from-file=jwt-secret=jwt-private.pem \
  -n "$NS"
```

혹은 서명/검증 키를 분리하고 싶으면 login 네임스페이스/시크릿만 개인키, 나머지는 공개키를 넣어 같은 이름으로 생성한다.

```bash
# login 전용 (개인키)
oc create secret generic handbook-jwt \
  --from-file=jwt-secret=jwt-private.pem \
  -n handbook-login

# 그 외 서비스 (공개키)
oc create secret generic handbook-jwt \
  --from-file=jwt-secret=jwt-public.pem \
  -n handbook
```

### 3) 생성 후 로컬 파일 제거

```bash
rm jwt-private.pem jwt-public.pem
```

생성된 Secret은 네임스페이스 안에만 존재하며 Git에는 들어가지 않는다.

### 확인

```bash
oc get secret handbook-jwt -n "$NS" -o jsonpath='{.data.jwt-secret}' | base64 -d | head -3
```
`-----BEGIN ... KEY-----` 로 시작하면 정상.

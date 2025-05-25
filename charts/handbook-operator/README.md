# 기준정보관리 시스템 CI/CD 헬름 차트

privileged에서 실행되는 docker-dind 실행을 위해  
``` shell
oc label namespace handbook-operator pod-security.kubernetes.io/enforce=privileged --overwrite
```

```shell
oc create token deployer -n handbook-operator --duration=4294967296s
```
```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: handbook
  namespace: openshift-gitops
spec:
  generators:
    - list:
        elements:
          - bucket: handbook-test
            database: 192.168.1.211
            host: handbook.sayaya.cloud
            mode: test
            name: handbook-test
            stage: test
          - bucket: handbook
            database: 192.168.1.210
            host: handbook.sayaya.dev
            mode: ''
            name: handbook
            stage: prod
  template:
    metadata:
      annotations:
        kargo.akuity.io/authorized-stage: handbook-operator:handbook-{{`{{ stage }}`}}
      name: '{{`{{ name }}`}}'
    spec:
      destination:
        namespace: '{{`{{ name }}`}}'
        server: 'https://kubernetes.default.svc'
      project: handbook
      sources:
        - path: charts/handbook
          repoURL: 'https://github.com/sayaya1090/handbook.git'
          targetRevision: HEAD
          helm:
            parameters:
              - name: host
                value: '{{`{{ host }}`}}'
              - name: mode
                value: '{{`{{ mode }}`}}'
              - name: bucket.name
                value: '{{`{{ bucket }}`}}'
              - name: database.ip
                value: '{{`{{ database }}`}}'
              - forceString: true
                name: persist-type.image.name
                value: 'image-registry.openshift-image-registry.svc:5000/{{ .Release.Namespace }}/persist-type'
              - forceString: true
                name: search-type.image.name
                value: 'image-registry.openshift-image-registry.svc:5000/{{ .Release.Namespace }}/search-type'
      syncPolicy:
        syncOptions:
          - CreateNamespace=true
```
```yaml
kind: Secret
apiVersion: v1
metadata:
  name: image-registry-secret
  namespace: handbook-operator
  labels:
    kargo.akuity.io/cred-type: image
data:
  repoURL: ^image-registry\.openshift-image-registry\.svc
  repoURLIsRegex: true
  username: deployer
  password: <TOKEN>
type: Opaque
```


```shell
curl -k -X GET \
  -H "Authorization: Bearer sha256~" \
  -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
  https://image-registry.openshift-image-registry.svc:5000/v2/handbook-operator/persist/manifests/latest > manifest.json

curl -k -X PUT \
  -H "Authorization: Bearer sha256~" \
  -H "Content-Type: application/vnd.docker.distribution.manifest.v2+json" \
  -d @manifest.json \
  https://image-registry.openshift-image-registry.svc:5000/v2/handbook-operator/persist/manifests/test

```

test:
- 자동으로 올라옴
- warehouse 별도 관리

candidate:
- test에서 freight를 승급
- 매뉴얼 프로모트
    - freight에서 모듈 검출
    - github actions로 모듈명, 이미지 태그 전달
    - github actions에서 소스코드와 이미지에 태깅
        - 전달된 모듈명을 사용하여 소스코드에 모듈명@날짜 태그
        - 전될된 이미지 정보를 사용하여 이미지에 날짜 태그

prod
- 모듈별 날짜 태그로 자동 승급
- warehouse 별도 관리

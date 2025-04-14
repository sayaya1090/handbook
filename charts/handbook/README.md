# 기준정보관리 시스템 헬름 차트

```shell
oc get role pull-images -n handbook-operator -o yaml
oc auth can-i get imagestreams/layers -n handbook-operator --as=system:serviceaccount:handbook-test:default
```

```shell
openssl genrsa -out a.pem 2048
openssl rsa -in a.pem -pubout -out b.pem
oc create secret generic authentication-keypair \
  --from-file=private-key=a.pem \
  --from-file=public-key=b.pem
```
# 기준정보관리 시스템 헬름 차트

```shell
oc get role pull-images -n handbook-operator -o yaml
oc auth can-i get imagestreams/layers -n handbook-operator --as=system:serviceaccount:handbook-test:default
```
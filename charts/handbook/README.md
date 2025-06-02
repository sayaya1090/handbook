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

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: handbook-test
  namespace: openshift-gitops
spec:
  destination:
    namespace: handbook-test
    server: 'https://kubernetes.default.svc'
  project: handbook
  sources:
    - helm:
        parameters:
          - name: host
            value: handbook.sayaya.cloud
          - name: mode
            value: test
          - name: bucket.name
            value: handbook-test
          - name: database.ip
            value: 192.168.1.211
          - forceString: true
            name: persist-type.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/persist-type'
          - forceString: true
            name: search-type.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/search-type'
          - name: search-type.image.tag
            value: 'latest@sha256:239999244255cf8748efaaaed1d5948f41ad1aee9512e6f3c76e605053b7a61e'
          - forceString: true
            name: gateway.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/gateway'
          - name: gateway.image.tag
            value: 'latest@sha256:de3fe34ac7601d91c5eba8ff1f1ebf7d8256d06bf05f20f848babc3e42739c7b'
          - forceString: true
            name: login.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/login'
          - name: login.image.tag
            value: 'latest@sha256:71bee0990f508f6ed6cb6325a1186fef750a71c6750a0ae590943b82a03731fd'
          - name: search-workspace.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/search-workspace'
          - name: search-workspace.image.tag
            value: 'latest@sha256:78aad1417d79a53ba6fae01de128ff36b9de0341cf99b2241aeb62e76f9cac68'
          - name: search-user.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/search-user'
          - name: search-user.image.tag
            value: 'latest@sha256:66d7e342d80a4de5a46228b8d060859289a4ec0875ceadedd6cd7ec2560fe889'
          - name: persist-user.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/persist-user'
          - name: persist-user.image.tag
            value: 'latest@sha256:2142d9fa9999e70c16278d9e5ea920a027620878a212b9b4a334a43f8fa50e34'
          - name: persist-workspace.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/persist-workspace'
          - name: persist-workspace.image.tag
            value: 'latest@sha256:e60a297651b2d24d38d57c8905e945d666d7d3da8e3af033e4e51bcd06e7cc95'
          - name: persist-type.image.tag
            value: 'latest@sha256:e3b49ca2a36c9550925309cb00da15150c9d8809948730d930c8dac0e3c31cdf'
          - name: persist-document.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/persist-document'
          - name: persist-document.image.tag
            value: 'latest@sha256:0baf17e059492d577c3a87f70c55ee166569cf2ffb73c866c7b965f99bbb9eb4'
          - name: search-document.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/search-document'
          - name: search-document.image.tag
            value: 'latest@sha256:7c0e1d199c1445ad61af625ac4e251d21017e9da235e86506aed6cd5c9bbb176'
          - name: validator.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/validator'
          - name: validator.image.tag
            value: 'latest@sha256:945e40e7f06a6c04d6698da7b104d811fc1253f2baacd8c2af0a3817ee25ea5b'
          - forceString: true
            name: event-broadcaster.image.name
            value: 'image-registry.openshift-image-registry.svc:5000/handbook-operator/event-broadcaster'
          - name: event-broadcaster.image.tag
            value: 'latest@sha256:ac84c4c82d0756a54b37cae4a77be79b31738cddcd7987b00dbab57dfa5c77ef'
      path: charts/handbook
      repoURL: 'https://github.com/sayaya1090/handbook.git'
      targetRevision: HEAD
  syncPolicy:
    syncOptions:
      - CreateNamespace=true
```
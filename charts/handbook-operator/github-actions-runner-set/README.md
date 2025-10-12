```yaml
kind: Secret
apiVersion: v1
metadata:
  name: github-secret
  namespace: github-actions-runner
data:
  github_app_id: ..
  github_app_installation_id: ..
  github_app_private_key: ..
type: Opaque
```


oc annotate rolebinding -n handbook-operator handbook-operator-6559f6d7-listener argocd.argoproj.io/compare-options=IgnoreExtraneous
oc annotate role handbook-operator-6559f6d7-listener argocd.argoproj.io/compare-options=IgnoreExtraneous

oc adm policy add-scc-to-user privileged -z arc -n handbook-operator
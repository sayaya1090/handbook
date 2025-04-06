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
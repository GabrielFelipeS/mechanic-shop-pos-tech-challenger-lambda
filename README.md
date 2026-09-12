# cpf-login-lambda

AWS Lambda function that logs a customer in by CPF, for the `mechanic-shop-pos-tech-challenger` project. Validates the CPF, checks the customer's existence/status against the main app's internal endpoint, and issues a JWT compatible with the app's existing `TokenService` (same secret/issuer/HS256/subject=email).

## Handler

`org.project.mechanic_shop.cpflogin.CpfLoginRequestHandler::handleRequest`

Expects an API Gateway (or Kong `aws-lambda` plugin, with `awsgateway_compatible = true`) proxy-integration event: `POST` with a JSON body `{"document": "<cpf>"}`.

| Response | Meaning |
|---|---|
| `200 {"token": "..."}` | CPF valid, customer exists, active |
| `400` | Missing/malformed body, or invalid CPF checksum |
| `404` | CPF valid but no active `CUSTOMER` found for it |
| `405` | Method other than `POST` |
| `502` | Could not reach the internal customer-status endpoint |

## Required environment variables

- `JWT_SECRET` — must match `api.security.token.secret` on the main app and the `jwt_secrets[].secret` configured in Kong.
- `JWT_ISSUER` — defaults to `mechanic-shop-api` if unset; must match the main app's issuer.
- `INTERNAL_API_BASE_URL` — base URL of the main app (e.g. its internal load balancer/service DNS), reachable from wherever this Lambda runs (needs network access into the app's VPC/cluster).
- `INTERNAL_API_SECRET` — must match `internal.api.secret` on the main app.

## Build

```bash
mvn test      # unit tests (Mockito-based, no AWS/network needed)
mvn package   # produces target/cpf-login-lambda.jar, a shaded deployment package
```

## Deploy

CI/CD (`.github/workflows/ci-cd.yml`) builds and tests on every push/PR to `main` or `master`. On a push it uploads an immutable deployment package to S3 and updates the already-provisioned Lambda. It uses GitHub OIDC: configure `AWS_ROLE_TO_ASSUME` as a repository secret; configure `AWS_REGION`, `LAMBDA_ARTIFACT_BUCKET`, `LAMBDA_ARTIFACT_PREFIX`, and `LAMBDA_FUNCTION_NAME` as repository variables. The Kubernetes-infrastructure Terraform repository provisions the function and API Gateway.

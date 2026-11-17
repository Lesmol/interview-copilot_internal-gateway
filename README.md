# interview-copilot_internal-gateway

Entry to the backend. Spring Cloud Gateway (WebFlux) on Spring Boot 4.1 / Java 25.

- Validates the BFF's service token (scope `interview-copilot/service`).
- Keeps `X-User-Id` only when the token also has scope `interview-copilot/act-as-user`; otherwise strips it.
- Adds `X-Request-Id`; passes streams through unbuffered.

| Route | Target | Required scope |
| --- | --- | --- |
| `/api/**` | `BACKEND_URL` | `interview-copilot/service` |
| `/actuator/health/**` | local | none |

Configuration is environment variables only; see `.env.example`.

```
./mvnw verify
```

Licence: Apache-2.0.

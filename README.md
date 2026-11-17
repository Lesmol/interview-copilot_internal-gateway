# interview-copilot_internal-gateway

Entry to the backend. Spring Cloud Gateway (WebFlux) on Spring Boot 4.1 / Java 25.

- Validates the BFF's service token (scope `interview-copilot/service`).
- Keeps `X-User-Id` only when the token also has scope `interview-copilot/act-as-user`; otherwise strips it.
- Adds `X-Request-Id`; passes streams through unbuffered.

| Route | Target | Required scope |
| --- | --- | --- |
| `/api/v1/**` | `BACKEND_URL` | `interview-copilot/service` |
| `/actuator/health/**` | local | none |

Configuration is environment variables only; see `.env.example`.

```
./mvnw verify
```

Licence: Apache-2.0.

## API versioning

Every HTTP API is versioned in the path: `/api/v1/...`. The version in the path is the contract version, separate from the artifact version. Each OpenAPI document carries the same contract version in `info.version` (currently `1.0.0`).

- Compatible changes (new endpoints, new optional fields) keep `/api/v1` and bump the minor or patch of `info.version`.
- A breaking change adds `/api/v2` next to `/api/v1`; both are served until consumers have moved, and the old one is removed in a later release.
- The gateways route only `/api/v1/**`, so a version nobody has deployed yet is a clean 404 at the edge, not a surprise further in.

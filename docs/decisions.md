# Decisions

- Spring Cloud Gateway (WebFlux), same product as the edge gateway.
- No default response timeout, so long-lived streams are not cut; rely on client and network timeouts.

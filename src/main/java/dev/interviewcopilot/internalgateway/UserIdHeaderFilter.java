package dev.interviewcopilot.internalgateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Keeps {@code X-User-Id} only when the service token carries the act-as-user scope. */
@Component
class UserIdHeaderFilter implements GlobalFilter, Ordered {

    static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(UserIdHeaderFilter::mayActAsUser)
                .defaultIfEmpty(false)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    }
                    var stripped = exchange.getRequest().mutate()
                            .headers(h -> h.remove(USER_ID_HEADER)).build();
                    return chain.filter(exchange.mutate().request(stripped).build());
                });
    }

    private static boolean mayActAsUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(("SCOPE_" + SecurityConfig.ACT_AS_USER_SCOPE)::equals);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}

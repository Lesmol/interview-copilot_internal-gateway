package dev.interviewcopilot.internalgateway;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Adds {@code X-Request-Id} when the caller did not send one. */
@Component
class RequestIdFilter implements GlobalFilter, Ordered {

    static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getHeaders().containsHeader(REQUEST_ID_HEADER)) {
            return chain.filter(exchange);
        }
        var request = exchange.getRequest().mutate()
                .header(REQUEST_ID_HEADER, UUID.randomUUID().toString()).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}

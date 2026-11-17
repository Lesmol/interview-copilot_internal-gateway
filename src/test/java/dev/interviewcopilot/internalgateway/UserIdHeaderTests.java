package dev.interviewcopilot.internalgateway;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class UserIdHeaderTests {

    static HttpServer backend;

    @Autowired
    WebTestClient client;

    @BeforeAll
    static void startBackend() throws IOException {
        backend = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        backend.createContext("/api/v1/echo", exchange -> {
            String userId = exchange.getRequestHeaders().getFirst("X-User-Id");
            byte[] body = ("user=" + userId).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        backend.start();
    }

    @AfterAll
    static void stopBackend() {
        backend.stop(0);
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("BACKEND_URL", () -> "http://127.0.0.1:" + backend.getAddress().getPort());
    }

    @Test
    void rejectsMissingToken() {
        client.get().uri("/api/v1/echo").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void rejectsTokenWithoutServiceScope() {
        client.mutateWith(mockJwt().authorities(() -> "SCOPE_other"))
                .get().uri("/api/v1/echo").exchange().expectStatus().isForbidden();
    }

    @Test
    void stripsUserIdWithoutActAsUserScope() {
        client.mutateWith(mockJwt().authorities(() -> "SCOPE_interview-copilot/service"))
                .get().uri("/api/v1/echo").header("X-User-Id", "forged").exchange()
                .expectStatus().isOk().expectBody(String.class).isEqualTo("user=null");
    }

    @Test
    void forwardsUserIdWithActAsUserScope() {
        client.mutateWith(mockJwt().authorities(() -> "SCOPE_interview-copilot/service",
                        () -> "SCOPE_interview-copilot/act-as-user"))
                .get().uri("/api/v1/echo").header("X-User-Id", "u1").exchange()
                .expectStatus().isOk().expectBody(String.class).isEqualTo("user=u1");
    }
}

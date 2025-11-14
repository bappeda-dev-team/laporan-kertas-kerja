package cc.kertaskerja.laporan.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class ServiceTokenProvider {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReentrantLock lock = new ReentrantLock();

    @Value("${keycloak.auth-server-url}")
    private String keycloakBase;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.service-client.client-id}")
    private String clientId;

    @Value("${keycloak.service-client.client-secret}")
    private String clientSecret;

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public String getToken() {
        lock.lock();
        try {
            if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(20))) {
                return cachedToken;
            }

            // else: ambil baru
            log.info("🔐 Requesting new Keycloak service token...");
            return fetchNewToken();

        } finally {
            lock.unlock();
        }
    }

    private String fetchNewToken() {
        String url = "%s/realms/%s/protocol/openid-connect/token"
                .formatted(keycloakBase, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        log.info("GET KEYCLOAK TOKEN:");
        log.info(url);

        try {
            long start = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            long exec = System.currentTimeMillis() - start;

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to get service token: HTTP " + response.getStatusCode());
            }

            KeycloakTokenResponse tokenResponse =
                    objectMapper.readValue(response.getBody(), KeycloakTokenResponse.class);

            cachedToken = tokenResponse.getAccessToken();
            expiresAt = Instant.now().plusSeconds(tokenResponse.getExpiresIn());

            log.info("🔑 Token OK (expires in {}s, took {}ms)", tokenResponse.getExpiresIn(), exec);

            return cachedToken;

        } catch (Exception e) {
            log.error("❌ Error fetching service token", e);
            throw new RuntimeException("Cannot fetch service token", e);
        }
    }

}

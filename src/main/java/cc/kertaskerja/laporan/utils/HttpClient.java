package cc.kertaskerja.laporan.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class HttpClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public <T> T post(String url, Object body, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        ResponseEntity<T> response = restTemplate.postForEntity(url, request, responseType);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("POST request failed: " + response.getStatusCode());
        }
    }

    public <T> T get(String url, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("GET request failed: " + response.getStatusCode());
        }
    }
}

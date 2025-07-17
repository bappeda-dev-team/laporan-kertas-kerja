package cc.kertaskerja.laporan.service.global;

import cc.kertaskerja.laporan.utils.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final RedisTokenService redisTokenService;
    private final HttpClient httpClient;

    public String getAccessToken() {
        String cachedToken = redisTokenService.getAccessToken();
        if (cachedToken != null && !cachedToken.isEmpty()) {
            return cachedToken;
        }

        String tokenUrl = "https://api-ekak.zeabur.app/user/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "username", "akun_test_level_3",
                "password", "KabKabMadiun2024"
        );

        Map<String, Object> response = httpClient.post(tokenUrl, body, headers, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");

        if (data != null && data.get("token") != null) {
            String accessToken = (String) data.get("token");
            redisTokenService.saveAccessToken(accessToken);
            return accessToken;
        } else {
            throw new RuntimeException("Token not found in response body");
        }
    }
}

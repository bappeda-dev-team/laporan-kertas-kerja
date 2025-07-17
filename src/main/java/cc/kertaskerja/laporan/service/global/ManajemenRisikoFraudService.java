package cc.kertaskerja.laporan.service.global;

import cc.kertaskerja.laporan.utils.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManajemenRisikoFraudService {

    private final AccessTokenService accessTokenService;
    private final HttpClient httpClient;

    @Value("@{external.manrisk-fraud.base-url}")
    private String manriskFraudUrl;

    public Map<String, Object> getDetailManriskFraud(String idRekin) {
        String token = accessTokenService.getAccessToken();
        String url = String.format("%s/analisa/get-detail/%s", manriskFraudUrl, idRekin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return httpClient.get(url, headers, Map.class);
    }
}

package cc.kertaskerja.laporan.service.global;

import cc.kertaskerja.laporan.utils.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RencanaKinerjaService {

    private final AccessTokenService accessTokenService;
    private final HttpClient httpClient;

    @Value("${external.rekin.base-url}")
    private String rekinBaseUrl;

    public Map<String, Object> getRencanaKinerjaOPD(String kodeOpd, String tahun) {
        String token = accessTokenService.getAccessToken();
        String url = String.format("%s/api_internal/rencana_kinerja/findall?kode_opd=%s&tahun=%s", rekinBaseUrl, kodeOpd, tahun);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return httpClient.get(url, headers, Map.class);
    }

    public Map<String, Object> getDetailRencanaKinerja(String idRekin) {
        String token = accessTokenService.getAccessToken();
        String url = String.format("%s/detail-rencana_kinerja/%s", rekinBaseUrl, idRekin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return httpClient.get(url, headers, Map.class);
    }

    public Map<String, Object> getAllRencanaKinerjaAtasan(String idRekin) {
        String token = accessTokenService.getAccessToken();
        String url = String.format("%s/rekin/atasan/%s", rekinBaseUrl, idRekin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return httpClient.get(url, headers, Map.class);
    }
}

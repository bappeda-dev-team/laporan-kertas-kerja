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

    public Map<String, Object> getRencanaKinerja(String nip, String tahun) {
        String token = accessTokenService.getAccessToken();
        String url = String.format("%s/get_rencana_kinerja/pegawai/%s?tahun=%s", rekinBaseUrl, nip, tahun);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return httpClient.get(url, headers, Map.class);
    }

    public Map<String, Object> getDetailRencanaKinerja(String idManrisk) {
        String token = accessTokenService.getAccessToken();
        String url = String.format("%s/detail-rencana_kinerja/%s", rekinBaseUrl, idManrisk);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return httpClient.get(url, headers, Map.class);
    }
}

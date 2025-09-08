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
        String url = String.format("%s/api_internal/rencana_kinerja/findall?kode_opd=%s&tahun=%s", rekinBaseUrl, kodeOpd, tahun);
        return get(url);
    }

    public Map<String, Object> getDetailRencanaKinerjaByNIP(String nip, String tahun) {
        String url = String.format("%s/get_rencana_kinerja/pegawai/%s?tahun=%s", rekinBaseUrl, nip, tahun);
        return get(url);
    }

    public Map<String, Object> getAllRencanaKinerjaAtasan(String idRekin) {
        String url = String.format("%s/rekin/atasan/%s", rekinBaseUrl, idRekin);
        return get(url);
    }

    /**
     * 🔹 Private helper to avoid repeating headers + token
     */
    private Map<String, Object> get(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenService.getAccessToken());
        return httpClient.get(url, headers, Map.class);
    }
}

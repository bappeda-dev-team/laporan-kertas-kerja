package cc.kertaskerja.laporan.service.global;

import cc.kertaskerja.laporan.service.external.DetailRekinResponseDTO;
import cc.kertaskerja.laporan.utils.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RencanaKinerjaService {

    private final HttpClient httpClient;
    private final RestTemplate restTemplate;

    @Value("${external.rekin.base-url}")
    private String rekinBaseUrl;

    public Map<String, Object> getRencanaKinerjaOPD(String sessionId, String kodeOpd, String tahun) {
        String url = String.format("%s/api_internal/rencana_kinerja/findall?kode_opd=%s&tahun=%s", rekinBaseUrl, kodeOpd, tahun);

        return get(sessionId, url);
    }

    public Map<String, Object> getDetailRencanaKinerjaByNIP(String sessionId, String nip, String tahun) {
        String url = String.format("%s/get_rencana_kinerja/pegawai/%s?tahun=%s", rekinBaseUrl, nip, tahun);
        return get(sessionId, url);
    }

    public Map<String, Object> getAllRencanaKinerjaAtasan(String sessionId, String idRekin) {
        String url = String.format("%s/rekin/atasan/%s", rekinBaseUrl, idRekin);
        return get(sessionId, url);
    }

    public DetailRekinResponseDTO getDetailRekin(String sessionId, String idRekin) {
        String url = String.format("%s/detail-rencana_kinerja/%s", rekinBaseUrl, idRekin);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Session-Id", sessionId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<DetailRekinResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, DetailRekinResponseDTO.class);

        return response.getBody();
    }

    /**
     * 🔹 Private helper to avoid repeating headers + token
     */
    private Map<String, Object> get(String sessionId, String url) {
        HttpHeaders headers = new HttpHeaders();
        //headers.setBearerAuth(accessTokenService.getAccessToken());
        headers.set("X-Session-Id", sessionId);
        return httpClient.get(url, headers, Map.class);
    }
}

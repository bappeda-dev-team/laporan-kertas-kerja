package cc.kertaskerja.laporan.service.global;

import cc.kertaskerja.laporan.dto.global.DetailRekinPegawaiResDTO;
import cc.kertaskerja.laporan.dto.global.RekinOpdByTahunResDTO;
import cc.kertaskerja.laporan.security.ServiceTokenProvider;
import cc.kertaskerja.laporan.service.external.DetailRekinResponseDTO;
import cc.kertaskerja.laporan.service.external.RekinFromPokinResponseDTO;
import cc.kertaskerja.laporan.utils.HttpClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RencanaKinerjaService {

    private final HttpClient httpClient;
    private final RestTemplate restTemplate;
    private final RedisService redisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ServiceTokenProvider serviceTokenProvider;

    @Value("${external.rekin.base-url}")
    @SuppressWarnings("unused")
    private String rekinBaseUrl;

    public Map<String, Object> getRencanaKinerjaOPD(String sessionId, String kodeOpd, String tahun) {
        String url = String.format("%s/api_internal/rencana_kinerja/findall?kode_opd=%s&tahun=%s", rekinBaseUrl, kodeOpd, tahun);

        return get(sessionId, url);
    }

    public RekinOpdByTahunResDTO findAllRekinOpdByTahun(String sessionId, String kodeOpd, String tahun) {
//        String cacheKey = "rekin:opd:%s:%s".formatted(kodeOpd, tahun);
        String url = String.format("%s/cascading_opd/multi_rekin_detail_by_opd_and_tahun/%s/%s", rekinBaseUrl, kodeOpd, tahun);

        // cek redis
//        String cachedJson = redisService.getRekinResponse(cacheKey);
//        if (cachedJson != null) {
//            try {
//                RekinOpdByTahunResDTO cached = objectMapper.readValue(cachedJson, RekinOpdByTahunResDTO.class);
//                log.info("✅ [CACHE HIT] findAllRekinOpdByTahun -> {}", cacheKey);
//                return cached;
//            } catch (Exception e) {
//                log.warn("⚠️ Gagal parse cache rekin OPD {}", cacheKey, e);
//            }
//        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Session-Id", sessionId);

        String serviceToken = serviceTokenProvider.getToken();
        headers.set("Authorization", "Bearer %s".formatted(serviceToken));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

//        long start = System.currentTimeMillis();
        ResponseEntity<RekinOpdByTahunResDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                RekinOpdByTahunResDTO.class
        );
//        long duration = System.currentTimeMillis() - start;
//        log.info("🌐 [API FETCH] findAllRekinOpdByTahun ({} ms)", duration);

        // hasil
        RekinOpdByTahunResDTO body = response.getBody();
        if (body == null) {
            log.warn("⚠️ Response kosong dari API rekin OPD {}", url);
            return new RekinOpdByTahunResDTO();
        }

        // simpan ke redis
//        try {
//            redisService.saveRekinResponse(cacheKey, objectMapper.writeValueAsString(body));
//            log.info("💾 [CACHE STORE] rekin OPD -> {}", cacheKey);
//        } catch (Exception e) {
//            log.error("❌ Gagal simpan cache rekin OPD {}", cacheKey, e);
//        }

        return body;
    }

    private record DetailRekinsRequest(
            @JsonProperty("rekin_ids")
            List<String> rekinIds
    ) {}

    public DetailRekinPegawaiResDTO detailRekins(String sessionId, List<String> rekinIds) {
        if (rekinIds == null || rekinIds.isEmpty()) return new DetailRekinPegawaiResDTO();

        String idsHash = Integer.toHexString(String.join(",", rekinIds).hashCode());
        String cacheKey = "rekin:detail:%s".formatted(idsHash);
        String url = String.format("%s/cascading_opd/findbymultiplerekin", rekinBaseUrl);

        // cek cache
        String cachedJson = redisService.getRekinResponse(cacheKey);
        if (cachedJson != null) {
            try {
                DetailRekinPegawaiResDTO cached = objectMapper.readValue(cachedJson, DetailRekinPegawaiResDTO.class);
                log.info("✅ [CACHE HIT] detailRekins -> {}", cacheKey);
                return cached;
            } catch (Exception e) {
                log.warn("⚠️ Gagal parse cache detailRekins {}", cacheKey, e);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Session-Id", sessionId);

        String serviceToken = serviceTokenProvider.getToken();
        headers.set("Authorization", "Bearer %s".formatted(serviceToken));

        DetailRekinsRequest requestBody = new DetailRekinsRequest(rekinIds);

        HttpEntity<DetailRekinsRequest> entity = new HttpEntity<>(requestBody, headers);

        long start = System.currentTimeMillis();
        ResponseEntity<DetailRekinPegawaiResDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DetailRekinPegawaiResDTO.class
        );
        long duration = System.currentTimeMillis() - start;
        log.info("🌐 [API FETCH] detailRekins ({} ms, {} ids)", duration, rekinIds.size());

        DetailRekinPegawaiResDTO body = response.getBody();
        if (body == null) {
            log.warn("⚠️ Response kosong dari API detailRekins {}", url);
            return new DetailRekinPegawaiResDTO();
        }

        try {
            redisService.saveRekinResponse(cacheKey, objectMapper.writeValueAsString(body));
            log.info("💾 [CACHE STORE] detailRekins -> {}", cacheKey);
        } catch (Exception e) {
            log.error("❌ Gagal simpan cache detailRekins {}", cacheKey, e);
        }

        return body;
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

        String serviceToken = serviceTokenProvider.getToken();
        headers.set("Authorization", "Bearer %s".formatted(serviceToken));


        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<DetailRekinResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, DetailRekinResponseDTO.class);

        return response.getBody();
    }

    public RekinFromPokinResponseDTO getRekinFromPokin(String sessionId, String idRekin) {
        String url = String.format("%s/cascading_opd/findbyrekin/%s", rekinBaseUrl, idRekin);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Session-Id", sessionId);

        String serviceToken = serviceTokenProvider.getToken();
        headers.set("Authorization", "Bearer %s".formatted(serviceToken));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<RekinFromPokinResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, RekinFromPokinResponseDTO.class);

        return response.getBody();
    }

    /**
     * 🔹 Private helper to avoid repeating headers + token
     */
    private Map<String, Object> get(String sessionId, String url) {
        HttpHeaders headers = new HttpHeaders();
        //headers.setBearerAuth(accessTokenService.getAccessToken());
        headers.set("X-Session-Id", sessionId);
        String serviceToken = serviceTokenProvider.getToken();
        headers.set("Authorization", "Bearer %s".formatted(serviceToken));

        return httpClient.get(url, headers, Map.class);
    }

    public void evictRekinCache(String kodeOpd, String tahun) {
        String keyOpd = "rekin:opd:%s:%s".formatted(kodeOpd, tahun);
        redisService.deleteRekinResponse(keyOpd);
        log.info("🗑️ Cache dihapus untuk key {}", keyOpd);
    }
}

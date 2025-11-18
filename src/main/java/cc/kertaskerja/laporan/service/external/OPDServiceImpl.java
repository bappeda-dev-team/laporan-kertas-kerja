package cc.kertaskerja.laporan.service.external;

import cc.kertaskerja.laporan.dto.global.RekinOpdByTahunResDTO;
import cc.kertaskerja.laporan.security.ServiceTokenProvider;
import cc.kertaskerja.laporan.service.global.RedisService;
import cc.kertaskerja.laporan.utils.HttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OPDServiceImpl implements OPDService {

    private final HttpClient httpClient;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Autowired
    private ServiceTokenProvider serviceTokenProvider;

    @Value("${external.pemda}")
    private String pemdaName;

    @Value("${external.rekin.base-url}")
    private String rekinBaseUrl;

    @Override
    public List<Map<String, Object>> findAllOPD(String sessionId) {
        String cacheKey = "opd:findall:%s".formatted(pemdaName);
        String url = "%s/opd/findall".formatted(rekinBaseUrl);

        // 1️⃣ Cek Redis
        try {
            String cachedJson = redisService.getRekinResponse(cacheKey);
            if (cachedJson != null) {
                return objectMapper.readValue(
                        cachedJson,
                        new TypeReference<List<Map<String, Object>>>() {}
                );
            }
        } catch (Exception e) {
            log.warn("⚠️ Gagal parse cache Redis untuk key {}", cacheKey, e);
        }

        // 2️⃣ Siapkan header
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Session-Id", sessionId);

        String serviceToken = serviceTokenProvider.getToken();
        headers.set("Authorization", "Bearer %s".formatted(serviceToken));

        // 3️⃣ Hit API eksternal
        Map<String, Object> response = httpClient.get(url, headers, Map.class);

        try {
            Object dataRaw = response.get("data");

            // Convert ke JSON
            String jsonData = objectMapper.writeValueAsString(dataRaw);

            // 4️⃣ Simpan ke Redis (opsional set TTL internal di redisService)
            redisService.saveRekinResponse(cacheKey, jsonData);

            // 5️⃣ Konversi ke list
            return objectMapper.readValue(
                    jsonData,
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            log.error("❌ Gagal parsing response OPD", e);
            return Collections.emptyList();
        }
    }
}


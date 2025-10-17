package cc.kertaskerja.laporan.service.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JabatanService {
    private final RestTemplate restTemplate;
    private final String kepegawaianBaseUrl;

    public JabatanService(RestTemplate restTemplate,
                          @Value("${external.kepegawaian.base-url}") String kepegawaianBaseUrl) {
        this.restTemplate = restTemplate;
        this.kepegawaianBaseUrl = kepegawaianBaseUrl;
    }

    public JabatanResponseDTO jabatanUser(String sessionId, String nipUser) {
        String urlJabatan = String.format("%s/pegawai/detail/%s", kepegawaianBaseUrl, nipUser);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Session-Id", sessionId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JabatanResponseDTO> response = restTemplate.exchange(urlJabatan, HttpMethod.GET, entity, JabatanResponseDTO.class);

        return response.getBody();
    }
}

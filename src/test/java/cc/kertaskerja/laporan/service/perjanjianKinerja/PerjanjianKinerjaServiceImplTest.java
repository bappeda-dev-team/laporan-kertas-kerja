package cc.kertaskerja.laporan.service.perjanjianKinerja;

import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.enums.StatusEnum;
import cc.kertaskerja.laporan.helper.Crypto;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import cc.kertaskerja.laporan.repository.VerifikatorRepository;
import cc.kertaskerja.laporan.service.PerjanjianKinerja.PerjanjianKinerjaServiceImpl;
import cc.kertaskerja.laporan.service.global.RedisService;
import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerjanjianKinerjaServiceImplTest {

    @Mock
    private RencanaKinerjaAtasanRepository rekinAtasanRepository;
    @Mock private VerifikatorRepository verifikatorRepository;
    @Mock private RencanaKinerjaService rencanaKinerjaService;
    @Mock private RedisService redisService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PerjanjianKinerjaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PerjanjianKinerjaServiceImpl(
                rekinAtasanRepository,
                verifikatorRepository,
                rencanaKinerjaService,
                redisService
        );
    }

    @Test
    void shouldReturnCachedDataFromRedis() throws Exception {
        String kodeOpd = "5.01";
        String tahun = "2025";
        String cacheKey = "rekin:" + kodeOpd + ":" + tahun;

        Map<String, Object> data = Map.of(
                "pegawai_id", "123456",
                "nama_pegawai", "Ryan",
                "operasional_daerah", Map.of("kode_opd", kodeOpd, "nama_opd", "Bappeda")
        );
        String json = objectMapper.writeValueAsString(List.of(data));

        when(redisService.getRekinResponse(cacheKey)).thenReturn(json);
        when(verifikatorRepository.findAll()).thenReturn(List.of());
        when(rekinAtasanRepository.findAll()).thenReturn(List.of());

        var result = service.findAllRencanaKinerja("sess", kodeOpd, tahun, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNama()).isEqualTo("Ryan");
        verifyNoInteractions(rencanaKinerjaService);
    }

    @Test
    void shouldFetchFromApiWhenCacheMissing() throws Exception {
        String kodeOpd = "5.01";
        String tahun = "2025";
        String cacheKey = "rekin:" + kodeOpd + ":" + tahun;

        when(redisService.getRekinResponse(cacheKey)).thenReturn(null);

        Map<String, Object> data = Map.of(
                "pegawai_id", "321",
                "nama_pegawai", "Luhur",
                "operasional_daerah", Map.of("kode_opd", kodeOpd, "nama_opd", "Bappeda"),
                "id_rencana_kinerja", "R1"
        );

        when(rencanaKinerjaService.getRencanaKinerjaOPD(any(), any(), any()))
                .thenReturn(Map.of("rencana_kinerja", List.of(data)));

        when(verifikatorRepository.findAll()).thenReturn(List.of());
        when(rekinAtasanRepository.findAll()).thenReturn(List.of());

        var result = service.findAllRencanaKinerja("sess", kodeOpd, tahun, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNama()).isEqualTo("Luhur");
        verify(redisService).saveRekinResponse(eq(cacheKey), any());
    }

    @Test
    void shouldFilterByLevelPegawai() throws Exception {
        String kodeOpd = "5.01";
        String tahun = "2025";
        String cacheKey = "rekin:" + kodeOpd + ":" + tahun;

        Map<String, Object> lowLevel = Map.of(
                "pegawai_id", "123",
                "nama_pegawai", "LowLevel",
                "level_pohon", 4,
                "operasional_daerah", Map.of("kode_opd", kodeOpd, "nama_opd", "Bappeda")
        );
        Map<String, Object> highLevel = Map.of(
                "pegawai_id", "999",
                "nama_pegawai", "HighLevel",
                "level_pohon", 6,
                "operasional_daerah", Map.of("kode_opd", kodeOpd, "nama_opd", "Bappeda")
        );

        String json = objectMapper.writeValueAsString(List.of(lowLevel, highLevel));

        when(redisService.getRekinResponse(cacheKey)).thenReturn(json);
        when(verifikatorRepository.findAll()).thenReturn(List.of());
        when(rekinAtasanRepository.findAll()).thenReturn(List.of());

        var result = service.findAllRencanaKinerja("sess", kodeOpd, tahun, "6");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNama()).isEqualTo("HighLevel");
    }

    @Test
    void shouldHandleDuplicateVerifikatorKeys() throws Exception {
        String kodeOpd = "5.01";
        String tahun = "2025";
        String cacheKey = "rekin:" + kodeOpd + ":" + tahun;
        String decryptedNip = "198012052009031002";

        Map<String, Object> data = Map.of(
                "pegawai_id", decryptedNip,
                "nama_pegawai", "Ryan",
                "operasional_daerah", Map.of("kode_opd", kodeOpd, "nama_opd", "Bappeda")
        );

        String json = objectMapper.writeValueAsString(List.of(data));

        Verifikator v1 = Verifikator.builder()
                .id(1L)
                .nip(Crypto.encrypt(decryptedNip))
                .namaAtasan("Luhur")
                .kodeOpd(kodeOpd)
                .namaOpd("Bappeda")
                .status(StatusEnum.PENDING)
                .levelPegawai(6)
                .tahunVerifikasi(2025)
                .build();

        Verifikator v2 = Verifikator.builder()
                .id(2L)
                .nip(Crypto.encrypt(decryptedNip)) // duplicate key
                .namaAtasan("Budi")
                .kodeOpd(kodeOpd)
                .namaOpd("Bappeda")
                .status(StatusEnum.PENDING)
                .levelPegawai(6)
                .tahunVerifikasi(2025)
                .build();

        when(redisService.getRekinResponse(cacheKey)).thenReturn(json);
        when(verifikatorRepository.findAll()).thenReturn(List.of(v1, v2)); // duplicate
        when(rekinAtasanRepository.findAll()).thenReturn(List.of());

        var result = service.findAllRencanaKinerja("sess", kodeOpd, tahun, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getVerifikator().getNama_atasan()).isEqualTo("Luhur");
    }
}

package cc.kertaskerja.laporan.dto.perjanjianKinerja;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RencanaKinerjaResDTO {
    private String kode_opd;
    private String nama_opd;
    private String nip;
    private String nama;
    private VerifikatorDTO verifikator;
    private List<RencanaKinerjaDetailDTO> rencana_kinerja;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RencanaKinerjaDetailDTO {
        private Long id;
        private String id_rencana_kinerja;
        private Integer id_pohon;
        private String nama_pohon;
        private Integer level_pohon;
        private String nama_rencana_kinerja;
        private String tahun;
        private String status_rencana_kinerja;
        private List<Map<String, Object>> indikator;
        private RencanaKinerjaAtasanDTO rencana_kinerja_atasan;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RencanaKinerjaAtasanDTO {
        private String nama;
        private String id_rencana_kinerja;
        private String nama_rencana_kinerja;
        private String kode_program;
        private String program;
        private Integer pagu_anggaran;
        private String indikator;
        private String target;
        private String satuan;
        private String status_rencana_kinerja;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifikatorDTO {
        private String kode_opd;
        private String nama_opd;
        private String nip;
        private String nama_atasan;
        private String nip_atasan;
        private Integer level_pegawai;
        private String status;
        private Integer tahun_verifikasi;
    }
}




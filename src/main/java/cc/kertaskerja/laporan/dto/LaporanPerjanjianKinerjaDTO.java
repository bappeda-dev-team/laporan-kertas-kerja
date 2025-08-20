package cc.kertaskerja.laporan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaporanPerjanjianKinerjaDTO {

    @JsonProperty("id_rencana_kinerja")
    private String idRencanaKinerja;

    @JsonProperty("nama_rencana_kinerja")
    private String namaRencanaKinerja;

    private String tahun;

    @JsonProperty("status_rencana_kinerja")
    private String statusRencanaKinerja;

    @JsonProperty("operasional_daerah")
    private OperasionalDaerahDTO operasionalDaerah;

    @JsonProperty("nip")
    private String nip;

    @JsonProperty("nama_pegawai")
    private String namaPegawai;

    private List<IndikatorDTO> indikator;

    @Data
    @Builder
    public static class OperasionalDaerahDTO {
        @JsonProperty("kode_opd")
        private String kodeOpd;

        @JsonProperty("nama_opd")
        private String namaOpd;
    }

    @Data
    @Builder
    public static class IndikatorDTO {
        @JsonProperty("id_indikator")
        private String idIndikator;

        @JsonProperty("nama_indikator")
        private String namaIndikator;

        private List<TargetDTO> targets;
    }

    @Data
    @Builder
    public static class TargetDTO {
        @JsonProperty("id_target")
        private String idTarget;

        @JsonProperty("indikator_id")
        private String indikatorId;

        private String target;
        private String satuan;
    }
}

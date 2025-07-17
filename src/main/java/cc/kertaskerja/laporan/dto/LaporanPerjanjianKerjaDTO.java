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
public class LaporanPerjanjianKerjaDTO {

    @JsonProperty("id_rencana_kinerja")
    private String idRencanaKinerja;

    @JsonProperty("id_pohon")
    private Integer idPohon;

    @JsonProperty("nama_pohon")
    private String namaPohon;

    @JsonProperty("nama_rencana_kinerja")
    private String namaRencanaKinerja;

    private String tahun;

    @JsonProperty("status_rencana_kinerja")
    private String statusRencanaKinerja;

    @JsonProperty("operasional_daerah")
    private OperasionalDaerahDTO operasionalDaerah;

    @JsonProperty("pegawai_id")
    private String pegawaiId;

    @JsonProperty("nama_pegawai")
    private String namaPegawai;

    private List<IndikatorDTO> indikator;

    @Data
    public static class OperasionalDaerahDTO {
        @JsonProperty("kode_opd")
        private String kodeOpd;

        @JsonProperty("nama_opd")
        private String namaOpd;
    }

    @Data
    public static class IndikatorDTO {
        @JsonProperty("id_indikator")
        private String idIndikator;

        @JsonProperty("nama_indikator")
        private String namaIndikator;

        private List<TargetDTO> targets;

        @JsonProperty("manual_ik_exist")
        private boolean manualIkExist;
    }

    @Data
    public static class TargetDTO {
        @JsonProperty("id_target")
        private String idTarget;

        @JsonProperty("indikator_id")
        private String indikatorId;

        private String target;
        private String satuan;
    }
}

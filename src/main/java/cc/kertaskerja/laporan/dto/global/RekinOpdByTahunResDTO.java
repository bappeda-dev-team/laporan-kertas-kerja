package cc.kertaskerja.laporan.dto.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RekinOpdByTahunResDTO {
    private int code;
    private String status;

    @JsonProperty("rencana_kinerja")
    private List<RencanaKinerja> rencanaKinerja;

    @Data
    public static class RencanaKinerja {

        @JsonProperty("id_rencana_kinerja")
        private String idRencanaKinerja;

        @JsonProperty("id_pohon")
        private Integer idPohon;

        @JsonProperty("nama_pohon")
        private String namaPohon;

        @JsonProperty("level_pohon")
        private Integer levelPohon;

        @JsonProperty("nama_rencana_kinerja")
        private String namaRencanaKinerja;

        private String tahun;

        @JsonProperty("status_rencana_kinerja")
        private String statusRencanaKinerja;

        @JsonProperty("operasional_daerah")
        private OperasionalDaerah operasionalDaerah;

        @JsonProperty("pegawai_id")
        private String pegawaiId;

        @JsonProperty("nama_pegawai")
        private String namaPegawai;

        private List<Indikator> indikator;
    }

    @Data
    public static class OperasionalDaerah {
        @JsonProperty("kode_opd")
        private String kodeOpd;

        @JsonProperty("nama_opd")
        private String namaOpd;
    }

    @Data
    public static class Indikator {

        @JsonProperty("id_indikator")
        private String idIndikator;

        @JsonProperty("rencana_kinerja_id")
        private String rencanaKinerjaId;

        @JsonProperty("nama_indikator")
        private String namaIndikator;

        private List<Target> targets;

        @JsonProperty("manual_ik_exist")
        private boolean manualIkExist;
    }

    @Data
    public static class Target {
        @JsonProperty("id_target")
        private String idTarget;

        @JsonProperty("indikator_id")
        private String indikatorId;

        private String target;
        private String satuan;
    }
}

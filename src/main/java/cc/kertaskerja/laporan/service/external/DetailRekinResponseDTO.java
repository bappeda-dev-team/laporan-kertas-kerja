package cc.kertaskerja.laporan.service.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DetailRekinResponseDTO {

    private int code;
    private String status;

    @JsonProperty("rencana_kinerja")
    private RencanaKinerjaItem rencanaKinerja;

    @Data
    public static class RencanaKinerjaItem {

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
        private OperasionalDaerah operasionalDaerah;

        @JsonProperty("pegawai_id")
        private String pegawaiId;

        @JsonProperty("nama_pegawai")
        private String namaPegawai;

        private List<IndikatorRekin> indikator;

        @Data
        public static class OperasionalDaerah {
            @JsonProperty("kode_opd")
            private String kodeOpd;

            @JsonProperty("nama_opd")
            private String namaOpd;
        }

        @Data
        public static class IndikatorRekin {

            @JsonProperty("id_indikator")
            private String idIndikator;

            @JsonProperty("nama_indikator")
            private String namaIndikator;

            private List<TargetIndikator> targets;

            @JsonProperty("manual_ik_exist")
            private boolean manualIkExist;

            @Data
            public static class TargetIndikator {

                @JsonProperty("id_target")
                private String idTarget;

                @JsonProperty("indikator_id")
                private String indikatorId;

                private String target;
                private String satuan;
            }
        }
    }
}

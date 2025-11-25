package cc.kertaskerja.laporan.dto.global;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RekinOpdByTahunResDTO {
    private int code;
    private String status;

    @JsonProperty("data")
    private List<RencanaKinerja> data;

    @Data
    public static class RencanaKinerja {

        @JsonProperty("id_rencana_kinerja")
        private String idRencanaKinerja;

        @JsonProperty("id_pohon")
        private Integer idPohon;

        @JsonProperty("level_pohon")
        private Integer levelPohon;

        @JsonProperty("nama_rencana_kinerja")
        private String namaRencanaKinerja;

        private String tahun;

        @JsonProperty("pegawai_id")
        private String pegawaiId;

        @JsonProperty("nama_pegawai")
        private String namaPegawai;

        @JsonProperty("kode_opd")
        private String kodeOpd;

        private List<Indikator> indikator;

        private List<Program> program;

        private List<Kegiatan> kegiatan;

        @JsonProperty("sub_kegiatan")
        private List<SubKegiatan> subKegiatan;

        @JsonProperty("pagu_anggaran_total")
        private Long paguAnggaranTotal;
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
    }

    @Data
    public static class Target {
        @JsonProperty("id_target")
        private String idTarget;

        @JsonProperty("indikator_id")
        private String indikatorId;

        private String target;
        private String satuan;
        private String tahun;
    }

    @Data
    public static class Program {
        @JsonProperty("kode_program")
        private String kodeProgram;

        @JsonProperty("nama_program")
        private String namaProgram;
    }

    @Data
    public static class Kegiatan {
        @JsonProperty("kode_kegiatan")
        private String kodeKegiatan;

        @JsonProperty("nama_kegiatan")
        private String namaKegiatan;
    }

    @Data
    public static class SubKegiatan {
        @JsonProperty("kode_subkegiatan")
        private String kodeSubkegiatan;

        @JsonProperty("nama_subkegiatan")
        private String namaSubkegiatan;
    }
}

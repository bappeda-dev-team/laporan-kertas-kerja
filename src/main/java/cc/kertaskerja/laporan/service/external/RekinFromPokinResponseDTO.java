package cc.kertaskerja.laporan.service.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RekinFromPokinResponseDTO {

    private int code;
    private String status;

    @JsonProperty("data")
    private Pokin data;

    @Data
    public static class Pokin {
        private Long id;
        private Long parent;

        @JsonProperty("nama_pohon")
        private String namaPohon;

        @JsonProperty("jenis_pohon")
        private String jenisPohon;

        @JsonProperty("level_pohon")
        private int levelPohon;

        private String keterangan;
        private String status;

        @JsonProperty("perangkat_daerah")
        private PerangkatDaerah perangkatDaerah;

        @JsonProperty("is_active")
        private boolean isActive;

        @JsonProperty("rencana_kinerja")
        private List<RencanaKinerja> rencanaKinerja;

        private List<Program> program;
        private List<Kegiatan> kegiatan;

        @JsonProperty("sub_kegiatan")
        private List<SubKegiatan> subKegiatan;

        @JsonProperty("pagu_anggaran_total")
        private double paguAnggaranTotal;
    }

    @Data
    public static class PerangkatDaerah {
        @JsonProperty("kode_opd")
        private String kodeOpd;

        @JsonProperty("nama_opd")
        private String namaOpd;
    }

    @Data
    public static class RencanaKinerja {
        @JsonProperty("id_rencana_kinerja")
        private String idRencanaKinerja;

        @JsonProperty("id_pohon")
        private Long idPohon;

        @JsonProperty("nama_pohon")
        private String namaPohon;

        @JsonProperty("nama_rencana_kinerja")
        private String namaRencanaKinerja;

        private String tahun;

        @JsonProperty("pegawai_id")
        private String pegawaiId;

        @JsonProperty("nama_pegawai")
        private String namaPegawai;

        private List<Indikator> indikator;
    }

    @Data
    public static class Indikator {
        @JsonProperty("id_indikator")
        private String idIndikator;

        @JsonProperty("id_rekin")
        private String idRekin;

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

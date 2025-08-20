package cc.kertaskerja.laporan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerjanjianKinerjaReqDTO {

    @NotBlank(message = "Harap isi nama pegawai")
    @JsonProperty("nama_pegawai")
    private String nama_pegawai;

    @NotBlank(message = "ID Rencana Kinerja tidak boleh kosong!")
    @JsonProperty("id_rencana_kinerja")
    private String id_rencana_kinerja;

    @NotBlank(message = "Nama Rencana Kinerja wajib diisi!")
    @JsonProperty("nama_rencana_kinerja")
    private String nama_rencana_kinerja;

    @NotBlank(message = "Kode Program wajib diisi!")
    @JsonProperty("kode_program")
    private String kode_program;

    @NotBlank(message = "Program tidak boleh kosong")
    @JsonProperty("program")
    private String program;

    @NotBlank(message = "Kode Kegiatan wajib diisi!")
    @JsonProperty("kode_kegiatan")
    private String kode_kegiatan;

    @NotBlank(message = "Kegiatan tidak boleh kosong")
    @JsonProperty("kegiatan")
    private String kegiatan;

    @NotBlank(message = "Kode Sub Kegiatan wajib diisi!")
    @JsonProperty("kode_sub_kegiatan")
    private String kode_sub_kegiatan;

    @NotBlank(message = "Sub Kegiatan tidak boleh kosong")
    @JsonProperty("sub_kegiatan")
    private String sub_kegiatan;

    @JsonProperty("pagu_anggaran")
    private Integer pagu_anggaran;

    @NotBlank(message = "Indikator wajib diisi!")
    @JsonProperty("indikator")
    private String indikator;

    @NotBlank(message = "Target wajib diisi!")
    @JsonProperty("target")
    private String target;

    @NotBlank(message = "Satuan wajib diisi!")
    @JsonProperty("satuan")
    private String satuan;

    @JsonProperty("status_rencana_kinerja")
    private String status_rencana_kinerja;

    @NotBlank(message = "Nama atasan wajib diisi!")
    @JsonProperty("nama_atasan")
    private String nama_atasan;

    @NotBlank(message = "ID Rencana Kinerja Atasan wajib diisi!")
    @JsonProperty("id_rencana_kinerja_atasan")
    private String id_rencana_kinerja_atasan;

    @NotBlank(message = "Nama Rencana Kinerja Atasan wajib diisi!")
    @JsonProperty("nama_rencana_kinerja_atasan")
    private String nama_rencana_kinerja_atasan;

    @NotBlank(message = "Kode Program Atasan wajib diisi!")
    @JsonProperty("kode_program_atasan")
    private String kode_program_atasan;

    @NotBlank(message = "Program Atasan wajib diisi!")
    @JsonProperty("program_atasan")
    private String program_atasan;

    @NotBlank(message = "Kode Kegiatan Atasan wajib diisi!")
    @JsonProperty("kode_kegiatan_atasan")
    private String kode_kegiatan_atasan;

    @NotBlank(message = "Kegiatan Atasan wajib diisi!")
    @JsonProperty("kegiatan_atasan")
    private String kegiatan_atasan;

    @NotBlank(message = "Kode Sub Kegiatan Atasan wajib diisi!")
    @JsonProperty("kode_sub_kegiatan_atasan")
    private String kode_sub_kegiatan_atasan;

    @NotBlank(message = "Sub Kegiatan Atasan wajib diisi!")
    @JsonProperty("sub_kegiatan_atasan")
    private String sub_kegiatan_atasan;

    @JsonProperty("status_rencana_kinerja_atasan")
    private String status_rencana_kinerja_atasan;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerificationDTO {

        @NotBlank(message = "Status tidak boleh kosong")
        @JsonProperty("status")
        private String status;

        @JsonProperty("keterangan")
        private String keterangan;

        @NotBlank(message = "NIP pegawai verifikator tidak boleh kosong")
        @JsonProperty("nip_verifikator")
        private String nipVerifikator;
    }
}

package cc.kertaskerja.laporan.dto.perjanjianKinerja;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifikatorReqDTO {
    @NotBlank(message = "Harap isi kode OPD")
    @JsonProperty("kode_opd")
    private String kode_opd;

    @NotBlank(message = "Harap isi nama OPD")
    @JsonProperty("nama_opd")
    private String nama_opd;

    @NotBlank(message = "Nama atasan wajib diisi!")
    @JsonProperty("nama_atasan")
    private String nama_atasan;

    @NotBlank(message = "NIP tidak boleh kosong!")
    @JsonProperty("nip")
    private String nip;

    @NotBlank(message = "NIP atasan tidak boleh kosong!")
    @JsonProperty("nip_atasan")
    private String nip_atasan;

    @JsonProperty("level_pegawai")
    private Integer level_pegawai;

    @NotBlank(message = "Status tidak boleh kosong")
    @JsonProperty("status")
    private String status;

    @NotNull(message = "Tahun verifikasi tidak boleh kosong")
    @JsonProperty("tahun_verifikasi")
    private Integer tahun_verifikasi;
}

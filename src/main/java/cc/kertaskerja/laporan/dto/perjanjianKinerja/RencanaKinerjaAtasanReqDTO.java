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
public class RencanaKinerjaAtasanReqDTO {

    @NotBlank(message = "Harap isi nama atasan")
    @JsonProperty("nama")
    private String nama;

    @NotBlank(message = "NIP atasan tidak boleh koson!")
    @JsonProperty("nip")
    private String nip;

    @NotNull(message = "Level pegawai tidak boleh kosong!")
    @JsonProperty("level_pegawai")
    private Integer level_pegawai;

    @NotBlank(message = "ID Rencana Kinerja tidak boleh kosong!")
    @JsonProperty("id_rencana_kinerja")
    private String id_rencana_kinerja;

    @NotBlank(message = "Nama Rencana Kinerja wajib diisi!")
    @JsonProperty("nama_rencana_kinerja")
    private String nama_rencana_kinerja;

    @NotBlank(message = "ID Rencana Kinerja Bawahan tidak boleh kosong!")
    @JsonProperty("id_rencana_kinerja_bawahan")
    private String id_rencana_kinerja_bawahan;

    @NotBlank(message = "NIP Bawahan tidak boleh kosong!")
    @JsonProperty("nip_bawahan")
    private String nip_bawahan;
}

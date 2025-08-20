package cc.kertaskerja.laporan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RencanaKinerjaAtasanDTO {

    @JsonProperty("id_rencana_kinerja")
    private String id_rencana_kinerja;

    @JsonProperty("nama_rencana_kinerja")
    private String nama_rencana_kinerja;

    private String tahun;

    @JsonProperty("status_rencana_kinerja")
    private String status_rencana_kinerja;

    @JsonProperty("catatan")
    private String catatan;

    @JsonProperty("kode_opd")
    private String kode_opd;

    @JsonProperty("nip")
    private String nip;

    @JsonProperty("nama_pegawai")
    private String nama_pegawai;
}

package cc.kertaskerja.laporan.service.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class JabatanResponseDTO {
    @JsonProperty("nama_jabatan")
    private String namaJabatan;

    @JsonProperty("status_jabatan")
    private String statusJabatan;

    @JsonProperty("pangkat")
    private String pangkat;

    @JsonProperty("kode_opd")
    private String kodeOpd;

}

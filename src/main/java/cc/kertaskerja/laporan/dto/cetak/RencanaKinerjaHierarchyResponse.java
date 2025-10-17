package cc.kertaskerja.laporan.dto.cetak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RencanaKinerjaHierarchyResponse {
    @JsonProperty("nip_atasan")
    private String nipAtasan;

    @JsonProperty("jabatan_atasan")
    private String jabatanAtasan;

    @JsonProperty("nama_atasan")
    private String namaAtasan;

    @JsonProperty("nip_bawahan")
    private String nipBawahan;

    @JsonProperty("jabatan_bawahan")
    private String jabatanBawahan;

    @JsonProperty("nama_bawahan")
    private String namaBawahan;

    @JsonProperty("rencana_kinerjas")
    private List<RencanaKinerjaHierarchyDTO> rencanaKinerjas;

    // universal way to call
    // program / kegiatna / subkegiatan / bidang urusan
    @JsonProperty("items")
    private List<ItemRekins> itemRekins;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRekins {
        @JsonProperty("nama_item")
        private String namaItem;

        @JsonProperty("kode_item")
        private String kodeItem;

        @JsonProperty("pagu")
        private Integer pagu;
    }
}

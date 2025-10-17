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
public class RencanaKinerjaHierarchyDTO {
    @JsonProperty("id_rencana_kinerja_atasan")
    private String idRencanaKinerjaAtasan;
    @JsonProperty("rencana_kinerja_atasan")
    private String rencanaKinerjaAtasan;

    @JsonProperty("rekin_bawahans")
    private List<ChildDTO> rekinBawahans;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildDTO {
        @JsonProperty("id_rencana_kinerja_bawahan")
        private String idRencanaKinerjaBawahan;

        @JsonProperty("rencana_kinerja_bawahan")
        private String rencanaKinerjaBawahan;

        @JsonProperty("indikators")
        private List<IndikatorSasaran> indikatorSasarans;


        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class IndikatorSasaran {
            @JsonProperty("indikator")
            private String indikator;

            @JsonProperty("target")
            private String target;

            @JsonProperty("satuan")
            private String satuan;
        }
    }
}

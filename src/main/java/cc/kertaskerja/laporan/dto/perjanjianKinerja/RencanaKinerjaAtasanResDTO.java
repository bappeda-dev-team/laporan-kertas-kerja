package cc.kertaskerja.laporan.dto.perjanjianKinerja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RencanaKinerjaAtasanResDTO {
    private String nama;
    private String id_rencana_kinerja;
    private String nama_rencana_kinerja;
    private String kode_program;
    private String program;
    private Integer pagu_anggaran;
    private String indikator;
    private String target;
    private String satuan;
    private String status_rencana_kinerja;
}

package cc.kertaskerja.laporan.dto.perjanjianKinerja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifikatorResDTO {
    private String nama_atasan;
    private String nip_atasan;
}

package cc.kertaskerja.laporan.service.PerjanjianKerja;

import cc.kertaskerja.laporan.dto.LaporanPerjanjianKerjaDTO;

public interface PerjanjianKerjaService {
    LaporanPerjanjianKerjaDTO findOnePK(String idRekin);
}

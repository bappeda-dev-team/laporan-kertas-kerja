package cc.kertaskerja.laporan.service.PerjanjianKinerja;

import cc.kertaskerja.laporan.dto.LaporanPerjanjianKinerjaDTO;
import cc.kertaskerja.laporan.dto.PerjanjianKinerjaReqDTO;
import cc.kertaskerja.laporan.dto.RencanaKinerjaAtasanDTO;
import cc.kertaskerja.laporan.entity.PerjanjianKinerja;

import java.util.List;

public interface PerjanjianKinerjaService {
    LaporanPerjanjianKinerjaDTO findOnePK(String kodeOpd, String tahun);

    List<RencanaKinerjaAtasanDTO> findAllRekinAtasan(String idRekin);

    PerjanjianKinerja savePK(PerjanjianKinerjaReqDTO dto);
}

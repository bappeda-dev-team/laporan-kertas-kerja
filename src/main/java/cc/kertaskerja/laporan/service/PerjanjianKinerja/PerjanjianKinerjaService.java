package cc.kertaskerja.laporan.service.PerjanjianKinerja;

import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaAtasanReqDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaAtasanResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.VerifikatorReqDTO;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;

import java.util.List;

public interface PerjanjianKinerjaService {
    List<RencanaKinerjaResDTO> findAllRencanaKinerja(String kodeOpd, String tahun, String levelPegawai);

    List<RencanaKinerjaAtasanResDTO> findAllRencanaKinerjaAtasanByIdRekinPegawai(String idRekin);

    RencanaKinerjaResDTO pkRencanaKinerja(String nip, String tahun);

    Verifikator verification(VerifikatorReqDTO dto);

    RencanaKinerjaAtasan savePK(RencanaKinerjaAtasanReqDTO dto);
}

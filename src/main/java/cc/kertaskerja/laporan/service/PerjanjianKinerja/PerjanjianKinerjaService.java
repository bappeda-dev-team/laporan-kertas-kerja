package cc.kertaskerja.laporan.service.PerjanjianKinerja;

import cc.kertaskerja.laporan.dto.PegawaiInfo;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.*;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;

import java.util.List;

public interface PerjanjianKinerjaService {
    List<RencanaKinerjaResDTO> findAllRencanaKinerja(String sessionId, String kodeOpd, String tahun, String levelPegawai);

    List<RencanaKinerjaAtasanResDTO> findAllRencanaKinerjaAtasanByIdRekinPegawai(String sessionId, String idRekin);

    RencanaKinerjaResDTO pkRencanaKinerja(String sessionId, String nip, String tahun);

    Verifikator verification(VerifikatorReqDTO dto);

    List<VerifikatorResDTO> findAllVerifikatorByPegawai(String nip);

    RencanaKinerjaAtasan savePK(RencanaKinerjaAtasanReqDTO dto);

    List<PegawaiInfo> listAtasan(String encNip);

    Boolean existingRekinAtasan(String idRekinAtasan);

    RencanaKinerjaAtasan updatePK(RencanaKinerjaAtasanReqDTO reqDTO, String idRekinAtasan);
}
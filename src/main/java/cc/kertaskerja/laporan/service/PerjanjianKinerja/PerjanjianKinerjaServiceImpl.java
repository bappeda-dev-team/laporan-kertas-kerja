package cc.kertaskerja.laporan.service.PerjanjianKinerja;

import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaAtasanReqDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.VerifikatorReqDTO;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.enums.StatusEnum;
import cc.kertaskerja.laporan.exception.ResourceNotFoundException;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import cc.kertaskerja.laporan.repository.VerifikatorRepository;
import cc.kertaskerja.laporan.service.external.EncryptService;
import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerjanjianKinerjaServiceImpl implements PerjanjianKinerjaService {

    private final RencanaKinerjaAtasanRepository rekinAtasanRepository;
    private final VerifikatorRepository verifikatorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RencanaKinerjaService rencanaKinerjaService;
    private final EncryptService encryptService;

    @Override
    public List<RencanaKinerjaResDTO> findAllRencanaKinerja(String kodeOpd, String tahun) {
        Map<String, Object> rekinResponse = rencanaKinerjaService.getRencanaKinerjaOPD(kodeOpd, tahun);
        Object rkObj = rekinResponse.get("rencana_kinerja");

        if (!(rkObj instanceof List<?> rkList)) {
            throw new ResourceNotFoundException("No 'rencana_kinerja' data found");
        }

        List<Map<String, Object>> rekinList = (List<Map<String, Object>>) rkList;
        List<RencanaKinerjaAtasan> rekinAtasanDBResponse = rekinAtasanRepository.findAll();
        List<Verifikator> verifikatorList = verifikatorRepository.findAll();

        // Buat map verifikator by nip
        Map<String, Verifikator> verifikatorByNip = verifikatorList.stream()
              .collect(Collectors.toMap(Verifikator::getNip, v -> v));

        // Buat map atasan by id_rencana_kinerja_bawahan
        Map<String, RencanaKinerjaAtasan> atasanByBawahan = rekinAtasanDBResponse.stream()
              .collect(Collectors.toMap(RencanaKinerjaAtasan::getIdRencanaKinerjaBawahan, a -> a));

        // Grouping by pegawai (nip)
        Map<String, List<Map<String, Object>>> groupedByNip = rekinList.stream()
              .collect(Collectors.groupingBy(r -> (String) r.get("pegawai_id")));

        List<RencanaKinerjaResDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByNip.entrySet()) {
            String nip = entry.getKey();
            List<Map<String, Object>> pegawaiRekinList = entry.getValue();

            if (pegawaiRekinList.isEmpty()) continue;

            Map<String, Object> firstRekin = pegawaiRekinList.get(0);
            Map<String, Object> opd = (Map<String, Object>) firstRekin.get("operasional_daerah");

            // Mapping verifikator
            Verifikator verifikator = verifikatorByNip.get(nip);
            RencanaKinerjaResDTO.VerifikatorDTO verifikatorDTO = null;
            if (verifikator != null) {
                verifikatorDTO = RencanaKinerjaResDTO.VerifikatorDTO.builder()
                      .kode_opd(verifikator.getKodeOpd())
                      .nama_opd(verifikator.getNamaOpd())
                      .nip(encryptService.encrypt(verifikator.getNip()))
                      .nama_atasan(verifikator.getNamaAtasan())
                      .nip_atasan(encryptService.encrypt(verifikator.getNipAtasan()))
                      .level_pegawai(verifikator.getLevelPegawai())
                      .status(verifikator.getStatus().name())
                      .tahun_verifikasi(verifikator.getTahunVerifikasi())
                      .build();
            }

            List<RencanaKinerjaResDTO.RencanaKinerjaDetailDTO> rencanaKinerjaDetails = new ArrayList<>();

            for (Map<String, Object> rk : pegawaiRekinList) {
                String idRencanaKinerja = (String) rk.get("id_rencana_kinerja");

                // mapping atasan jika ada
                RencanaKinerjaAtasan atasan = atasanByBawahan.get(idRencanaKinerja);
                RencanaKinerjaResDTO.RencanaKinerjaAtasanDTO atasanDTO = null;
                if (atasan != null) {
                    atasanDTO = RencanaKinerjaResDTO.RencanaKinerjaAtasanDTO.builder()
                          .nama(atasan.getNama())
                          .id_rencana_kinerja(atasan.getIdRencanaKinerja())
                          .nama_rencana_kinerja(atasan.getNamaRencanaKinerja())
                          .kode_program(atasan.getKodeProgram())
                          .program(atasan.getProgram())
                          .pagu_anggaran(atasan.getPaguAnggaran())
                          .indikator(atasan.getIndikator())
                          .target(atasan.getTarget())
                          .satuan(atasan.getSatuan())
                          .build();
                }

                // mapping indikator (bisa kosong)
                List<Map<String, Object>> indikator = (List<Map<String, Object>>) rk.getOrDefault("indikator", new ArrayList<>());

                RencanaKinerjaResDTO.RencanaKinerjaDetailDTO detailDTO =
                      RencanaKinerjaResDTO.RencanaKinerjaDetailDTO.builder()
                            .id_rencana_kinerja(idRencanaKinerja)
                            .id_pohon((Integer) rk.get("id_pohon"))
                            .nama_pohon((String) rk.get("nama_pohon"))
                            .level_pohon((Integer) rk.get("level_pohon"))
                            .nama_rencana_kinerja((String) rk.get("nama_rencana_kinerja"))
                            .tahun((String) rk.get("tahun"))
                            .status_rencana_kinerja((String) rk.get("status_rencana_kinerja"))
                            .indikator(indikator)
                            .rencana_kinerja_atasan(atasanDTO)
                            .build();

                rencanaKinerjaDetails.add(detailDTO);
            }

            RencanaKinerjaResDTO dto = RencanaKinerjaResDTO.builder()
                  .kode_opd((String) opd.get("kode_opd"))
                  .nama_opd((String) opd.get("nama_opd"))
                  .nip(encryptService.encrypt(nip))
                  .nama((String) firstRekin.get("nama_pegawai"))
                  .verifikator(verifikatorDTO)
                  .rencana_kinerja(rencanaKinerjaDetails)
                  .build();

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public Verifikator verification(VerifikatorReqDTO dto) {
        Verifikator verifikator = Verifikator.builder()
              .kodeOpd(dto.getKode_opd())
              .namaOpd(dto.getNama_opd())
              .nip(dto.getNip())
              .namaAtasan(dto.getNama_atasan())
              .nipAtasan(dto.getNip_atasan())
              .levelPegawai(dto.getLevel_pegawai())
              .status(StatusEnum.PENDING)
              .tahunVerifikasi(dto.getTahun_verifikasi())
              .build();

        return verifikatorRepository.save(verifikator);
    }

    @Override
    @Transactional
    public RencanaKinerjaAtasan savePK(RencanaKinerjaAtasanReqDTO dto) {
        RencanaKinerjaAtasan rekinAtasan = RencanaKinerjaAtasan.builder()
              .nama(dto.getNama())
              .nip(dto.getNip())
              .levelPegawai(dto.getLevel_pegawai())
              .idRencanaKinerja(dto.getId_rencana_kinerja())
              .namaRencanaKinerja(dto.getNama_rencana_kinerja())
              .idRencanaKinerjaBawahan(dto.getId_rencana_kinerja_bawahan())
              .kodeProgram(dto.getKode_program())
              .program(dto.getProgram()).kodeKegiatan(dto.getKode_kegiatan())
              .kegiatan(dto.getKegiatan())
              .kodeSubKegiatan(dto.getKode_sub_kegiatan())
              .subKegiatan(dto.getSub_kegiatan())
              .paguAnggaran(dto.getPagu_anggaran())
              .indikator(dto.getIndikator())
              .target(dto.getTarget())
              .satuan(dto.getSatuan())
              .statusRencanaKinerja(dto.getStatus_rencana_kinerja())
              .build();

        return rekinAtasanRepository.save(rekinAtasan);
    }
}

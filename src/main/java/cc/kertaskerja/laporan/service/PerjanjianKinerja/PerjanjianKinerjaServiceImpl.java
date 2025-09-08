package cc.kertaskerja.laporan.service.PerjanjianKinerja;

import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaAtasanReqDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaAtasanResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.RencanaKinerjaResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.VerifikatorReqDTO;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.enums.StatusEnum;
import cc.kertaskerja.laporan.exception.ResourceNotFoundException;
import cc.kertaskerja.laporan.helper.Crypto;
import cc.kertaskerja.laporan.helper.Format;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import cc.kertaskerja.laporan.repository.VerifikatorRepository;
import cc.kertaskerja.laporan.service.external.EncryptService;
import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

        // 🔑 Buat map verifikator by plain NIP (decrypt dulu nip di DB)
        Map<String, Verifikator> verifikatorByNip = verifikatorList.stream()
              .collect(Collectors.toMap(v -> Crypto.decrypt(v.getNip()), v -> v));

        // Buat map atasan by id_rencana_kinerja_bawahan
        Map<String, RencanaKinerjaAtasan> atasanByBawahan = rekinAtasanDBResponse.stream()
              .collect(Collectors.toMap(RencanaKinerjaAtasan::getIdRencanaKinerjaBawahan, a -> a));

        // Grouping by pegawai (nip dari API → plain text)
        Map<String, List<Map<String, Object>>> groupedByNip = rekinList.stream()
              .collect(Collectors.groupingBy(r -> (String) r.get("pegawai_id")));

        List<RencanaKinerjaResDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByNip.entrySet()) {
            String nip = entry.getKey(); // plain nip dari API
            List<Map<String, Object>> pegawaiRekinList = entry.getValue();

            if (pegawaiRekinList.isEmpty()) continue;

            Map<String, Object> firstRekin = pegawaiRekinList.get(0);
            Map<String, Object> opd = (Map<String, Object>) firstRekin.get("operasional_daerah");

            // 🔑 Mapping verifikator (lookup pakai nip plain)
            Verifikator verifikator = verifikatorByNip.get(nip);
            RencanaKinerjaResDTO.VerifikatorDTO verifikatorDTO = null;
            if (verifikator != null) {
                verifikatorDTO = RencanaKinerjaResDTO.VerifikatorDTO.builder()
                      .kode_opd(verifikator.getKodeOpd())
                      .nama_opd(verifikator.getNamaOpd())
                      .nip(verifikator.getNip()) // tetap encrypted biar aman
                      .nama_atasan(verifikator.getNamaAtasan())
                      .nip_atasan(verifikator.getNipAtasan())
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

            // 🔑 encrypt nip untuk response keluar
            RencanaKinerjaResDTO dto = RencanaKinerjaResDTO.builder()
                  .kode_opd((String) opd.get("kode_opd"))
                  .nama_opd((String) opd.get("nama_opd"))
                  .nip(Crypto.encrypt(nip)) // encrypt lagi
                  .nama((String) firstRekin.get("nama_pegawai"))
                  .verifikator(verifikatorDTO)
                  .rencana_kinerja(rencanaKinerjaDetails)
                  .build();

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<RencanaKinerjaAtasanResDTO> findAllRencanaKinerjaAtasanByIdRekinPegawai(String idRekin) {
        Map<String, Object> rekinAtasanResponse = rencanaKinerjaService.getAllRencanaKinerjaAtasan(idRekin);

        Map<String, Object> data = (Map<String, Object>) rekinAtasanResponse.get("data");
        List<Map<String, Object>> rekinAtasanList = (List<Map<String, Object>>) data.get("rekin_atasan");

        return rekinAtasanList.stream().map(item -> RencanaKinerjaAtasanResDTO.builder()
              .id_rencana_kinerja((String) item.get("id"))
              .nama((String) item.get("nama_pegawai"))
              .nama_rencana_kinerja((String) item.get("nama_rencana_kinerja"))
              .kode_program((String) item.get("kode_program"))
              .program((String) item.get("program"))
              .pagu_anggaran(Format.parseInteger(item.get("pagu_program")))
              .status_rencana_kinerja((String) item.get("status_rencana_kinerja"))
              .build()
        ).toList();
    }

    @Override
    public RencanaKinerjaResDTO pkRencanaKinerja(String nip, String tahun) {
        if (!Crypto.isEncrypted(nip)) {
            throw new ResourceNotFoundException("NIP is not encrypted: " + nip);
        }

        // 1. Decrypt NIP untuk ke API
        String plainNip = Crypto.decrypt(nip);

        // 2. Call external API
        Map<String, Object> rekinResponse = rencanaKinerjaService.getDetailRencanaKinerjaByNIP(plainNip, tahun);

        // 3. DB responses
        List<RencanaKinerjaAtasan> rekinAtasanDBResponse = rekinAtasanRepository.findByNipBawahan(nip); // pakai encrypted nip
        List<Verifikator> verifikatorList = verifikatorRepository.findAll();

        // 4. Extract rencana_kinerja list from API
        List<Map<String, Object>> rencanaKinerjaList = (List<Map<String, Object>>) rekinResponse.get("rencana_kinerja");
        if (rencanaKinerjaList == null || rencanaKinerjaList.isEmpty()) {
            throw new ResourceNotFoundException("No 'rencana_kinerja' data found for NIP: " + plainNip);
        }

        // 5. Map verifikator by plain NIP
        Map<String, Verifikator> verifikatorByNip = verifikatorList.stream()
              .collect(Collectors.toMap(v -> Crypto.decrypt(v.getNip()), v -> v));

        Verifikator verifikator = verifikatorByNip.get(plainNip);
        if (verifikator == null) {
            throw new ResourceNotFoundException("No verifikator found for NIP: " + plainNip);
        }

        RencanaKinerjaResDTO.VerifikatorDTO verifikatorDTO = RencanaKinerjaResDTO.VerifikatorDTO.builder()
              .kode_opd(verifikator.getKodeOpd())
              .nama_opd(verifikator.getNamaOpd())
              .nip(verifikator.getNip()) // tetap encrypted
              .nama_atasan(verifikator.getNamaAtasan())
              .nip_atasan(verifikator.getNipAtasan())
              .level_pegawai(verifikator.getLevelPegawai())
              .status(verifikator.getStatus().name())
              .tahun_verifikasi(verifikator.getTahunVerifikasi())
              .build();

        // 6. Pick OPD + Pegawai info from first element
        Map<String, Object> first = rencanaKinerjaList.get(0);
        Map<String, Object> opd = (Map<String, Object>) first.get("operasional_daerah");

        // 7. Map only rencana_kinerja that have a matching atasan
        List<RencanaKinerjaResDTO.RencanaKinerjaDetailDTO> detailList = rencanaKinerjaList.stream()
              .map(item -> {
                  // cari atasan dari DB
                  RencanaKinerjaAtasan atasan = rekinAtasanDBResponse.stream()
                        .filter(a -> a.getIdRencanaKinerjaBawahan().equals(item.get("id_rencana_kinerja")))
                        .findFirst()
                        .orElse(null);

                  if (atasan == null) {
                      return null; // skip kalau tidak ada atasan
                  }

                  RencanaKinerjaResDTO.RencanaKinerjaAtasanDTO atasanDTO =
                        RencanaKinerjaResDTO.RencanaKinerjaAtasanDTO.builder()
                              .nama(atasan.getNama())
                              .id_rencana_kinerja(atasan.getIdRencanaKinerja())
                              .nama_rencana_kinerja(atasan.getNamaRencanaKinerja())
                              .kode_program(atasan.getKodeProgram())
                              .program(atasan.getProgram())
                              .pagu_anggaran(atasan.getPaguAnggaran())
                              .indikator(atasan.getIndikator())
                              .target(atasan.getTarget())
                              .satuan(atasan.getSatuan())
                              .status_rencana_kinerja(atasan.getStatusRencanaKinerja())
                              .build();

                  return RencanaKinerjaResDTO.RencanaKinerjaDetailDTO.builder()
                        .id_rencana_kinerja((String) item.get("id_rencana_kinerja"))
                        .id_pohon((Integer) item.get("id_pohon"))
                        .nama_pohon((String) item.get("nama_pohon"))
                        .level_pohon((Integer) item.get("level_pohon"))
                        .nama_rencana_kinerja((String) item.get("nama_rencana_kinerja"))
                        .tahun((String) item.get("tahun"))
                        .status_rencana_kinerja((String) item.get("status_rencana_kinerja"))
                        .indikator((List<Map<String, Object>>) item.get("indikator"))
                        .rencana_kinerja_atasan(atasanDTO)
                        .build();
              })
              .filter(Objects::nonNull) // hanya ambil yang punya atasan
              .toList();

        // 8. Build DTO response
        return RencanaKinerjaResDTO.builder()
              .kode_opd((String) opd.get("kode_opd"))
              .nama_opd((String) opd.get("nama_opd"))
              .nip(nip) // encrypted
              .nama((String) first.get("nama_pegawai"))
              .verifikator(verifikatorDTO)
              .rencana_kinerja(detailList)
              .build();
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
              .nipBawahan(dto.getNip_bawahan())
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

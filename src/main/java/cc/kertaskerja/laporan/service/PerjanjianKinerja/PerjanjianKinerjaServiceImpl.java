package cc.kertaskerja.laporan.service.PerjanjianKinerja;

import cc.kertaskerja.laporan.dto.PegawaiInfo;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.*;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.enums.StatusEnum;
import cc.kertaskerja.laporan.exception.ResourceNotFoundException;
import cc.kertaskerja.laporan.helper.Crypto;
import cc.kertaskerja.laporan.helper.Format;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import cc.kertaskerja.laporan.repository.VerifikatorRepository;
import cc.kertaskerja.laporan.service.external.EncryptService;
import cc.kertaskerja.laporan.service.global.RedisService;
import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
    private final RedisService redisService;

    @Override
    public List<RencanaKinerjaResDTO> findAllRencanaKinerja(String sessionId, String kodeOpd, String tahun, String levelPegawai) {
        String cacheKey = String.format("rekin:%s:%s", kodeOpd, tahun);
        List<Map<String, Object>> rekinList;

        // 1️⃣ Coba ambil dari Redis
        String cachedJson = redisService.getRekinResponse(cacheKey);
        if (cachedJson != null) {
            try {
                rekinList = objectMapper.readValue(cachedJson, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse cached rekinList", e);
            }
        } else {
            // 2️⃣ Ambil dari API jika belum ada di Redis
            Map<String, Object> rekinResponse = rencanaKinerjaService.getRencanaKinerjaOPD(sessionId, kodeOpd, tahun);
            Object rkObj = rekinResponse.get("rencana_kinerja");

            if (!(rkObj instanceof List<?> rkList)) {
                throw new ResourceNotFoundException("Data not found");
            }

            rekinList = (List<Map<String, Object>>) rkList;

            // 3️⃣ Simpan ke Redis biar bisa dipakai ulang
            try {
                String json = objectMapper.writeValueAsString(rekinList);
                redisService.saveRekinResponse(cacheKey, json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to cache rekinList", e);
            }
        }

        // 🟢 4️⃣ Filter by levelPegawai (bandingkan ke field level_pohon dari API)
        if (levelPegawai != null && !levelPegawai.isBlank()) {
            try {
                int levelFilter = Integer.parseInt(levelPegawai);
                rekinList = rekinList.stream()
                      .filter(r -> {
                          Object levelObj = r.get("level_pohon");
                          return levelObj instanceof Integer && ((Integer) levelObj) == levelFilter;
                      })
                      .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("levelPegawai harus berupa angka", e);
            }
        }

        // 5️⃣ Lanjutkan logic mapping kamu
        List<RencanaKinerjaAtasan> rekinAtasanDBResponse = rekinAtasanRepository.findAll();
        List<Verifikator> verifikatorList = verifikatorRepository.findAll();

        Map<String, Verifikator> verifikatorByNip = verifikatorList.stream()
              .collect(Collectors.toMap(v -> Crypto.decrypt(v.getNip()), v -> v));

        Map<String, RencanaKinerjaAtasan> atasanByBawahan = rekinAtasanDBResponse.stream()
              .collect(Collectors.toMap(RencanaKinerjaAtasan::getIdRencanaKinerjaBawahan, a -> a));

        Map<String, List<Map<String, Object>>> groupedByNip = rekinList.stream()
              .collect(Collectors.groupingBy(r -> (String) r.get("pegawai_id")));

        List<RencanaKinerjaResDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByNip.entrySet()) {
            String nip = entry.getKey();
            List<Map<String, Object>> pegawaiRekinList = entry.getValue();
            if (pegawaiRekinList.isEmpty()) continue;

            Map<String, Object> firstRekin = pegawaiRekinList.get(0);
            Map<String, Object> opd = (Map<String, Object>) firstRekin.get("operasional_daerah");

            Verifikator verifikator = verifikatorByNip.get(nip);
            RencanaKinerjaResDTO.VerifikatorDTO verifikatorDTO = null;
            if (verifikator != null) {
                verifikatorDTO = RencanaKinerjaResDTO.VerifikatorDTO.builder()
                      .kode_opd(verifikator.getKodeOpd())
                      .nama_opd(verifikator.getNamaOpd())
                      .nip(verifikator.getNip())
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

                List<Map<String, Object>> indikator = (List<Map<String, Object>>) rk.getOrDefault("indikator", new ArrayList<>());

                rencanaKinerjaDetails.add(
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
                            .build()
                );
            }

            result.add(
                  RencanaKinerjaResDTO.builder()
                        .kode_opd((String) opd.get("kode_opd"))
                        .nama_opd((String) opd.get("nama_opd"))
                        .nip(Crypto.encrypt(nip))
                        .nama((String) firstRekin.get("nama_pegawai"))
                        .verifikator(verifikatorDTO)
                        .rencana_kinerja(rencanaKinerjaDetails)
                        .build()
            );
        }

        return result;
    }

    @Override
    public List<RencanaKinerjaAtasanResDTO> findAllRencanaKinerjaAtasanByIdRekinPegawai(String sessionId, String idRekin) {
        Map<String, Object> rekinAtasanResponse = rencanaKinerjaService.getAllRencanaKinerjaAtasan(sessionId, idRekin);
        Map<String, Object> data = (Map<String, Object>) rekinAtasanResponse.get("data");

        if (data == null || data.get("rekin_atasan") == null) {
            return List.of(); // ⬅️ return empty list, not null
        }

        List<Map<String, Object>> rekinAtasanList = (List<Map<String, Object>>) data.get("rekin_atasan");

        return rekinAtasanList.stream().map(item -> RencanaKinerjaAtasanResDTO.builder()
              .id_rencana_kinerja((String) item.get("id"))
              .nama((String) item.get("nama_pegawai"))
              .nip((String) item.get("nip"))
              .nama_rencana_kinerja((String) item.get("nama_rencana_kinerja"))
              .kode_program((String) item.get("kode_program"))
              .program((String) item.get("program"))
              .pagu_anggaran(Format.parseInteger(item.get("pagu_program")))
              .status_rencana_kinerja((String) item.get("status_rencana_kinerja"))
              .build()
        ).toList();
    }

    @Override
    public RencanaKinerjaResDTO pkRencanaKinerja(String sessionId, String nip, String tahun) {
        if (!Crypto.isEncrypted(nip)) {
            throw new ResourceNotFoundException("NIP is not encrypted: " + nip);
        }

        // 1. Decrypt NIP untuk ke API
        String plainNip = Crypto.decrypt(nip);

        // 2. Call external API
        Map<String, Object> rekinResponse = rencanaKinerjaService.getDetailRencanaKinerjaByNIP(sessionId, plainNip, tahun);

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
    public List<VerifikatorResDTO> findAllVerifikatorByPegawai(String nip) {
        List<Verifikator> verifikatorList = verifikatorRepository.findVerifikatorByNip(nip);

        return verifikatorList.stream()
              .map(v -> VerifikatorResDTO.builder()
                    .nama_atasan(v.getNamaAtasan())
                    .nip_atasan(v.getNipAtasan())
                    .build())
              .toList();
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
              .kodeProgram("-")
              .program("-")
              .kodeKegiatan("-")
              .kegiatan("-")
              .kodeSubKegiatan("-")
              .subKegiatan("-")
              .paguAnggaran(0)
              .indikator("-")
              .target("-")
              .satuan("-")
              .statusRencanaKinerja("UNCHECKED")
              .build();

        return rekinAtasanRepository.save(rekinAtasan);
    }

    @Override
    public List<PegawaiInfo> listAtasan(String encNip) {
        if (!Crypto.isEncrypted(encNip)) {
            throw new ResourceNotFoundException("NIP is not encrypted: " + encNip);
        }
        String nip = Crypto.decrypt(encNip);

        List<RencanaKinerjaAtasan> atasanByNip = rekinAtasanRepository.findByNipBawahan(nip);

        return atasanByNip.stream()
                .map(v -> PegawaiInfo.builder()
                        .nama(v.getNama())
                        .nip(v.getNip())
                        .build())
                .distinct()
                .toList();
    }
}

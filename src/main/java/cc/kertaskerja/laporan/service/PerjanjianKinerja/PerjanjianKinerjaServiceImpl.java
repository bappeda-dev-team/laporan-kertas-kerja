package cc.kertaskerja.laporan.service.PerjanjianKinerja;

import cc.kertaskerja.laporan.dto.PegawaiInfo;
import cc.kertaskerja.laporan.dto.global.DetailRekinPegawaiResDTO;
import cc.kertaskerja.laporan.dto.global.RekinOpdByTahunResDTO;
import cc.kertaskerja.laporan.dto.perjanjianKinerja.*;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.enums.StatusEnum;
import cc.kertaskerja.laporan.exception.ResourceNotFoundException;
import cc.kertaskerja.laporan.helper.Crypto;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import cc.kertaskerja.laporan.repository.VerifikatorRepository;
import cc.kertaskerja.laporan.service.external.RekinFromPokinResponseDTO;
import cc.kertaskerja.laporan.service.global.RedisService;
import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cc.kertaskerja.laporan.utils.PatchUtil.apply;

@Service
@RequiredArgsConstructor
public class PerjanjianKinerjaServiceImpl implements PerjanjianKinerjaService {

    private final RencanaKinerjaAtasanRepository rekinAtasanRepository;
    private final VerifikatorRepository verifikatorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RencanaKinerjaService rencanaKinerjaService;
    private final RedisService redisService;

    @Override
    public List<RencanaKinerjaResDTO> findAllRencanaKinerja(String sessionId, String kodeOpd, String tahun, String levelPegawai) {
        String cacheKey = "rekin:%s:%s".formatted(kodeOpd, tahun);
        List<Map<String, Object>> rekinList = getCachedRekin(cacheKey)
                .orElseGet(() -> fetchAndCacheRekin(sessionId, kodeOpd, tahun, cacheKey));

        if (levelPegawai != null && !levelPegawai.isBlank()) {
            int level = Integer.parseInt(levelPegawai);
            rekinList = rekinList.stream()
                    .filter(r -> level == (r.get("level_pohon") instanceof Integer lv ? lv : -1))
                    .toList();
        }

        Map<String, Verifikator> verifikatorByNip = verifikatorRepository.findAll().stream()
                .collect(Collectors.toMap(
                        v -> Crypto.decrypt(v.getNip()),
                        v -> v,
                        (v1, v2) -> v1 // 🧩 fix duplicate key error
                ));

        Map<String, RencanaKinerjaAtasan> atasanByBawahan = rekinAtasanRepository.findAll().stream()
                .collect(Collectors.toMap(
                        RencanaKinerjaAtasan::getIdRencanaKinerjaBawahan,
                        a -> a,
                        (a1, a2) -> a1
                ));

        return rekinList.stream()
                .collect(Collectors.groupingBy(r -> (String) r.get("pegawai_id")))
                .entrySet().stream()
                .map(e -> mapToResDTO(e.getKey(), e.getValue(), verifikatorByNip, atasanByBawahan))
                .toList();
    }

    private Optional<List<Map<String, Object>>> getCachedRekin(String cacheKey) {
        String json = redisService.getRekinResponse(cacheKey);
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {
            }));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse cached rekinList", e);
        }
    }

    private List<Map<String, Object>> fetchAndCacheRekin(String sessionId, String kodeOpd, String tahun, String cacheKey) {
        Map<String, Object> resp = rencanaKinerjaService.getRencanaKinerjaOPD(sessionId, kodeOpd, tahun);
        Object rkObj = resp.get("rencana_kinerja");
        if (!(rkObj instanceof List<?> rkList)) throw new ResourceNotFoundException("Data not found");

        try {
            String json = objectMapper.writeValueAsString(rkList);
            redisService.saveRekinResponse(cacheKey, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to cache rekinList", e);
        }
        return (List<Map<String, Object>>) rkList;
    }

    private RencanaKinerjaResDTO mapToResDTO(
            String nip,
            List<Map<String, Object>> list,
            Map<String, Verifikator> verifikatorByNip,
            Map<String, RencanaKinerjaAtasan> atasanByBawahan
    ) {
        Map<String, Object> first = list.getFirst();
        Map<String, Object> opd = (Map<String, Object>) first.get("operasional_daerah");
        Verifikator v = verifikatorByNip.get(nip);

        var verifikatorDTO = v == null ? null : RencanaKinerjaResDTO.VerifikatorDTO.builder()
                .kode_opd(v.getKodeOpd())
                .nama_opd(v.getNamaOpd())
                .nip(v.getNip())
                .nama_atasan(v.getNamaAtasan())
                .nip_atasan(v.getNipAtasan())
                .level_pegawai(v.getLevelPegawai())
                .status(v.getStatus().name())
                .tahun_verifikasi(v.getTahunVerifikasi())
                .build();

        List<RencanaKinerjaResDTO.RencanaKinerjaDetailDTO> details = list.stream()
                .map(r -> mapDetailDTO(r, atasanByBawahan))
                .toList();

        return RencanaKinerjaResDTO.builder()
                .kode_opd((String) opd.get("kode_opd"))
                .nama_opd((String) opd.get("nama_opd"))
                .nip(Crypto.encrypt(nip))
                .nama((String) first.get("nama_pegawai"))
                .verifikator(verifikatorDTO)
                .rencana_kinerja(details)
                .build();
    }

    private RencanaKinerjaResDTO.RencanaKinerjaDetailDTO mapDetailDTO(Map<String, Object> rk, Map<String, RencanaKinerjaAtasan> atasanByBawahan) {
        String id = (String) rk.get("id_rencana_kinerja");
        RencanaKinerjaAtasan a = atasanByBawahan.get(id);

        var atasanDTO = a == null ? null : RencanaKinerjaResDTO.RencanaKinerjaAtasanDTO.builder()
                .nama(a.getNama())
                .id_rencana_kinerja(a.getIdRencanaKinerja())
                .nama_rencana_kinerja(a.getNamaRencanaKinerja())
                .kode_program(a.getKodeProgram())
                .program(a.getProgram())
                .pagu_anggaran(a.getPaguAnggaran())
                .indikator(a.getIndikator())
                .target(a.getTarget())
                .satuan(a.getSatuan())
                .build();

        return RencanaKinerjaResDTO.RencanaKinerjaDetailDTO.builder()
                .id(Optional.ofNullable(a)
                        .map(RencanaKinerjaAtasan::getId)
                        .orElse(null))
                .id_rencana_kinerja(id)
                .id_pohon((Integer) rk.get("id_pohon"))
                .nama_pohon((String) rk.get("nama_pohon"))
                .level_pohon((Integer) rk.get("level_pohon"))
                .nama_rencana_kinerja((String) rk.get("nama_rencana_kinerja"))
                .tahun((String) rk.get("tahun"))
                .status_rencana_kinerja((String) rk.get("status_rencana_kinerja"))
                .indikator((List<Map<String, Object>>) rk.getOrDefault("indikator", List.of()))
                .rencana_kinerja_atasan(atasanDTO)
                .build();
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
                .id_rencana_kinerja((String) item.get("id_rencana_kinerja"))
                .nama((String) item.get("nama_pegawai"))
                .nip((String) item.get("pegawai_id"))
                .nama_rencana_kinerja((String) item.get("nama_rencana_kinerja"))
                .kode_program("-")
                .program("-")
                .pagu_anggaran(0)
                .status_rencana_kinerja((String) item.get("status_rencana_kinerja"))
                .build()
        ).toList();
    }

    // INI UNTUK APA YA ?
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
        Map<String, Object> first = rencanaKinerjaList.getFirst();
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
                .nama(dto.getNama())
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

    @Transactional
    @Override
    public RencanaKinerjaAtasan savePK(String sessionId, RencanaKinerjaAtasanReqDTO dto) {
        var idRekinBawahan = dto.getId_rencana_kinerja_bawahan();
        var findDetailBawahan = rencanaKinerjaService.getRekinFromPokin(sessionId, idRekinBawahan);

        AtomicReference<String> kodeProgram = new AtomicReference<>("-");
        AtomicReference<String> namaProgram = new AtomicReference<>("-");
        AtomicReference<String> kodeKegiatan = new AtomicReference<>("-");
        AtomicReference<String> namaKegiatan = new AtomicReference<>("-");
        AtomicReference<String> kodeSubkegiatan = new AtomicReference<>("-");
        AtomicReference<String> namaSubkegiatan = new AtomicReference<>("-");

        Optional.ofNullable(findDetailBawahan)
                .map(RekinFromPokinResponseDTO::getData)
                .ifPresent(data -> {
                    // program
                    Optional.ofNullable(data.getProgram())
                            .filter(list -> !list.isEmpty())
                            .map(List::getFirst)
                            .ifPresent(program -> {
                                kodeProgram.set(program.getKodeProgram());
                                namaProgram.set(program.getNamaProgram());
                            });

                    // kegiatan
                    Optional.ofNullable(data.getKegiatan())
                            .filter(list -> !list.isEmpty())
                            .map(List::getFirst)
                            .ifPresent(kegiatan -> {
                                kodeKegiatan.set(kegiatan.getKodeKegiatan());
                                namaKegiatan.set(kegiatan.getNamaKegiatan());
                            });

                    // sub kegiatan
                    Optional.ofNullable(data.getSubKegiatan())
                            .filter(list -> !list.isEmpty())
                            .map(List::getFirst)
                            .ifPresent(sub -> {
                                kodeSubkegiatan.set(sub.getKodeSubkegiatan());
                                namaSubkegiatan.set(sub.getNamaSubkegiatan());
                            });
                });

        var paguAnggaran = 0;
        var statusRencanaKinerja = "UNCHECKED";

        RencanaKinerjaAtasan rekinAtasan = RencanaKinerjaAtasan.builder()
                .nama(dto.getNama())
                .nip(dto.getNip())
                .levelPegawai(dto.getLevel_pegawai())
                .idRencanaKinerja(dto.getId_rencana_kinerja())
                .namaRencanaKinerja(dto.getNama_rencana_kinerja())
                .idRencanaKinerjaBawahan(dto.getId_rencana_kinerja_bawahan())
                .namaRencanaKinerjaBawahan(dto.getNama_rencana_kinerja_bawahan())
                .nipBawahan(dto.getNip_bawahan())
                .namaBawahan(dto.getNama_bawahan())
                .kodeProgram(String.valueOf(kodeProgram))
                .program(String.valueOf(namaProgram))
                .kodeKegiatan(String.valueOf(kodeKegiatan))
                .kegiatan(String.valueOf(namaKegiatan))
                .kodeSubKegiatan(String.valueOf(kodeSubkegiatan))
                .subKegiatan(String.valueOf(namaSubkegiatan))
                .paguAnggaran(paguAnggaran)
                .indikator("-")
                .target("-")
                .satuan("-")
                .statusRencanaKinerja(statusRencanaKinerja)
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
                        .nip(Crypto.encrypt(v.getNip()))
                        .build())
                .distinct()
                .toList();
    }

    @Override
    public Boolean existingRekinAtasan(String idRekinAtasan) {
        return rekinAtasanRepository.existsById(Long.parseLong(idRekinAtasan));
    }

    @Override
    public RencanaKinerjaAtasan updatePK(RencanaKinerjaAtasanReqDTO reqDTO, String idRekinAtasan) {
        var existing = rekinAtasanRepository.findById(Long.parseLong(idRekinAtasan))
                .orElseThrow(() -> new EntityNotFoundException("Rencana kinerja dengan ID " + idRekinAtasan + " tidak ditemukan"));

        // PatchUtil apply
        // set value jika terdapat di field reqDTO
        apply(reqDTO.getNama(), existing::setNama);
        apply(reqDTO.getLevel_pegawai(), existing::setLevelPegawai);
        apply(reqDTO.getId_rencana_kinerja(), existing::setIdRencanaKinerja);
        apply(reqDTO.getNama_rencana_kinerja(), existing::setNamaRencanaKinerja);
        apply(reqDTO.getId_rencana_kinerja_bawahan(), existing::setIdRencanaKinerjaBawahan);
        apply(reqDTO.getNama_rencana_kinerja_bawahan(), existing::setNamaRencanaKinerjaBawahan);
        apply(reqDTO.getNip_bawahan(), existing::setNipBawahan);
        apply(reqDTO.getNama_bawahan(), existing::setNamaBawahan);
        apply(reqDTO.getKode_program(), existing::setKodeProgram);
        apply(reqDTO.getProgram(), existing::setProgram);
        apply(reqDTO.getKode_kegiatan(), existing::setKodeKegiatan);
        apply(reqDTO.getKegiatan(), existing::setKegiatan);
        apply(reqDTO.getKode_sub_kegiatan(), existing::setKodeSubKegiatan);
        apply(reqDTO.getSub_kegiatan(), existing::setSubKegiatan);
        apply(reqDTO.getPagu_anggaran(), existing::setPaguAnggaran);

        return rekinAtasanRepository.save(existing);
    }

    @Override
    public RencanaKinerjaAtasan findById(String pkId) {
        return rekinAtasanRepository.findById(Long.parseLong(pkId))
                .orElseThrow(() -> new EntityNotFoundException("Rencana kinerja dengan ID " + pkId + " tidak ditemukan"));
    }

    @Override
    public void batalkan(String pkId) {
        rekinAtasanRepository.deleteById(Long.parseLong(pkId));
    }

    @Override
    public List<RencanaKinerjaResDTO> getAllRencanaKinerjaOpd(String sessionId, String kodeOpd, String tahun, String levelPegawai) {
        // guard level pegawai
        int tempLevel;
        try {
            tempLevel = (levelPegawai == null || levelPegawai.isBlank())
                    ? 0
                    : Integer.parseInt(levelPegawai);
        } catch (NumberFormatException ex) {
            tempLevel = 0;
        }
        final int levelPohonPegawai = tempLevel;

        // get rekin from api perencanan
        RekinOpdByTahunResDTO rekinApiPerencanaan = rencanaKinerjaService.findAllRekinOpdByTahun(sessionId, kodeOpd, tahun);

        if (rekinApiPerencanaan == null || rekinApiPerencanaan.getRencanaKinerja() == null) {
            return Collections.emptyList();
        }

        // kumpulkan rekin id
        List<String> rekinIds = rekinApiPerencanaan.getRencanaKinerja().stream()
                .filter(Objects::nonNull)
                .filter(r -> {
                    Integer level = r.getLevelPohon();
                    return levelPohonPegawai == 0 || (level != null && level == levelPohonPegawai);
                })
                .map(RekinOpdByTahunResDTO.RencanaKinerja::getIdRencanaKinerja)
                .toList();

        // get detail rekin
        DetailRekinPegawaiResDTO detailRekins = rencanaKinerjaService.detailRekins(sessionId, rekinIds);
        List<DetailRekinPegawaiResDTO.RencanaKinerja> rekinDetails = detailRekins.getData().stream()
                .filter(Objects::nonNull)
                .flatMap(dataItem -> {
                    List<DetailRekinPegawaiResDTO.RencanaKinerja> rk = Optional.ofNullable(dataItem.getRencanaKinerja())
                            .orElse(Collections.emptyList())
                            .stream()
                            .peek(rl -> rl.setLevelPohon(dataItem.getLevelPohon()))
                            .toList();

                    return rk.stream();
                })
                .toList();

        Map<String, Long> totalPagu = detailRekins.getData().stream()
                .filter(Objects::nonNull)
                .flatMap(dataItem -> {
                    List<String> idRekins = Optional.ofNullable(dataItem.getRencanaKinerja())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(DetailRekinPegawaiResDTO.RencanaKinerja::getIdRencanaKinerja)
                            .filter(Objects::nonNull)
                            .toList();

                    Long paguAnggaran = dataItem.getPaguAnggaranTotal();

                    return idRekins.stream()
                            .map(id -> Map.entry(id, paguAnggaran));

                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));


        Map<String, List<Object>> programKegiatanSubkegiatan = detailRekins.getData().stream()
                .filter(Objects::nonNull)
                .flatMap(dataItem -> {
                    // Semua idRencanaKinerja yang ada di dataItem
                    List<String> idRekins = Optional.ofNullable(dataItem.getRencanaKinerja())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(DetailRekinPegawaiResDTO.RencanaKinerja::getIdRencanaKinerja)
                            .filter(Objects::nonNull)
                            .toList();

                    // Gabungkan program, kegiatan, subkegiatan
                    List<Object> allItems = Stream.of(
                                    Optional.ofNullable(dataItem.getProgram()).orElse(Collections.emptyList()),
                                    Optional.ofNullable(dataItem.getKegiatan()).orElse(Collections.emptyList()),
                                    Optional.ofNullable(dataItem.getSubKegiatan()).orElse(Collections.emptyList())
                            )
                            .flatMap(List::stream)
                            .collect(Collectors.toList());

                    // Setiap idRencanaKinerja -> list yang sama (karena satu pohon punya banyak id)
                    return idRekins.stream()
                            .map(id -> Map.entry(id, allItems));
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        // Merge kalau ada key yang sama (gabungkan list-nya)
                        (list1, list2) -> {
                            List<Object> merged = new ArrayList<>(list1);
                            merged.addAll(list2);
                            return merged;
                        }
                ));


        // sebelah kanan
        Map<String, Verifikator> verifikatorByNip = verifikatorRepository.findAll().stream()
                .collect(Collectors.toMap(
                        v -> Crypto.decrypt(v.getNip()),
                        v -> v,
                        (v1, v2) -> v1 // 🧩 fix duplicate key error
                ));

        // sebelah kiri
        Map<String, RencanaKinerjaAtasan> atasanByBawahan = rekinAtasanRepository.findAll().stream()
                .collect(Collectors.toMap(
                        RencanaKinerjaAtasan::getIdRencanaKinerjaBawahan,
                        a -> a,
                        (a1, a2) -> a1
                ));

        // empty if rekinDetails kosong :(
        if (rekinDetails.isEmpty()) {
            return Collections.emptyList();
        }

        return rekinDetails.stream()
                .filter(Objects::nonNull)
                .filter(rk -> rk.getPegawaiId() != null)
                .collect(Collectors.groupingBy(DetailRekinPegawaiResDTO.RencanaKinerja::getPegawaiId))
                .entrySet().stream()
                .map(e -> {
                    // nip
                    String nip = e.getKey();
                    String nama = e.getValue().getFirst().getNamaPegawai();
                    // opd
                    String kode_opd = rekinApiPerencanaan.getRencanaKinerja().getFirst().getOperasionalDaerah().getKodeOpd();
                    String nama_opd = rekinApiPerencanaan.getRencanaKinerja().getFirst().getOperasionalDaerah().getNamaOpd();
                    Verifikator v = verifikatorByNip.get(nip);

                    RencanaKinerjaResDTO.VerifikatorDTO verifikator = v == null ? null : RencanaKinerjaResDTO.VerifikatorDTO.builder()
                            .kode_opd(v.getKodeOpd())
                            .nama_opd(v.getNamaOpd())
                            .nip(v.getNip())
                            .nama_atasan(v.getNamaAtasan())
                            .nip_atasan(v.getNipAtasan())
                            .level_pegawai(v.getLevelPegawai())
                            .status(v.getStatus().name())
                            .tahun_verifikasi(v.getTahunVerifikasi())
                            .build();

                    // rencana kinerja
                    List<RencanaKinerjaResDTO.RencanaKinerjaDetailDTO> rencanaKinerjaDetails = e.getValue().stream()
                            .map(rk -> {
                                String idRekin = rk.getIdRencanaKinerja();
                                RencanaKinerjaAtasan a = atasanByBawahan.get(idRekin);

                                var paguBawahan = totalPagu.get(idRekin);
                                var myProgramKegiatanSubkegiatan = programKegiatanSubkegiatan.get(idRekin);
                                List<RencanaKinerjaResDTO.Program> programs = new ArrayList<>();
                                List<RencanaKinerjaResDTO.Kegiatan> kegiatans = new ArrayList<>();
                                List<RencanaKinerjaResDTO.SubKegiatan> subkegiatans = new ArrayList<>();

                                if (myProgramKegiatanSubkegiatan != null) {
                                    for (Object obj : myProgramKegiatanSubkegiatan) {
                                        if (obj instanceof DetailRekinPegawaiResDTO.Program p) {
                                            programs.add(mapProgram(p));
                                        } else if (obj instanceof DetailRekinPegawaiResDTO.Kegiatan k) {
                                            kegiatans.add(mapKegiatan(k));
                                        } else if (obj instanceof DetailRekinPegawaiResDTO.SubKegiatan s) {
                                            subkegiatans.add(mapSubKegiatan(s));
                                        }
                                    }
                                }

                                RencanaKinerjaResDTO.RencanaKinerjaAtasanDTO atasanDTO = null;

                                if (a != null) {
                                    var programKegiatanAtasan = programKegiatanSubkegiatan.get(a.getIdRencanaKinerja());
                                    List<RencanaKinerjaResDTO.Program> programsAtaasan = new ArrayList<>();
                                    List<RencanaKinerjaResDTO.Kegiatan> kegiatansAtasan = new ArrayList<>();
                                    List<RencanaKinerjaResDTO.SubKegiatan> subkegiatansAtasan = new ArrayList<>();

                                    if (programKegiatanAtasan != null) {
                                        for (Object obj : programKegiatanAtasan) {
                                            if (obj instanceof DetailRekinPegawaiResDTO.Program p) {
                                                programsAtaasan.add(mapProgram(p));
                                            } else if (obj instanceof DetailRekinPegawaiResDTO.Kegiatan k) {
                                                kegiatansAtasan.add(mapKegiatan(k));
                                            } else if (obj instanceof DetailRekinPegawaiResDTO.SubKegiatan s) {
                                                subkegiatansAtasan.add(mapSubKegiatan(s));
                                            }
                                        }
                                    }

                                    var paguAtasan = totalPagu.get(a.getIdRencanaKinerja());

                                    atasanDTO = RencanaKinerjaResDTO.RencanaKinerjaAtasanDTO.builder()
                                            .nama(a.getNama())
                                            .id_rencana_kinerja(a.getIdRencanaKinerja())
                                            .nama_rencana_kinerja(a.getNamaRencanaKinerja())
                                            .kode_program(a.getKodeProgram())
                                            .program(a.getProgram())
                                            .pagu_anggaran(a.getPaguAnggaran())
                                            .indikator(a.getIndikator())
                                            .target(a.getTarget())
                                            .satuan(a.getSatuan())
                                            .paguAnggaranTotal(paguAtasan)
                                            .programs(programsAtaasan)
                                            .kegiatans(kegiatansAtasan)
                                            .subkegiatans(subkegiatansAtasan)
                                            .build();
                                }

                                return RencanaKinerjaResDTO.RencanaKinerjaDetailDTO.builder()
                                        .id(Optional.ofNullable(a)
                                                .map(RencanaKinerjaAtasan::getId)
                                                .orElse(null))
                                        .id_rencana_kinerja(idRekin)
                                        .id_pohon(Math.toIntExact(rk.getIdPohon()))
                                        .nama_pohon(rk.getNamaPohon())
                                        .level_pohon(rk.getLevelPohon())
                                        .nama_rencana_kinerja(rk.getNamaRencanaKinerja())
                                        .tahun(rk.getTahun())
                                        .status_rencana_kinerja("-")
                                        .paguAnggaranTotal(paguBawahan)
                                        .rencana_kinerja_atasan(atasanDTO)
                                        .programs(programs)
                                        .kegiatans(kegiatans)
                                        .subkegiatans(subkegiatans)
                                        .build();
                            })
                            .toList();

                    return RencanaKinerjaResDTO.builder()
                            .kode_opd(kode_opd)
                            .nama_opd(nama_opd)
                            .nip(Crypto.encrypt(nip))
                            .nama(nama)
                            .verifikator(verifikator)
                            .rencana_kinerja(rencanaKinerjaDetails)
                            .build();

                    //List<DetailRekinPegawaiResDTO.RencanaKinerja> rks = e.getValue();
                })
                .toList();
    }

    private RencanaKinerjaResDTO.Program mapProgram(DetailRekinPegawaiResDTO.Program src) {
        RencanaKinerjaResDTO.Program p = new RencanaKinerjaResDTO.Program();
        p.setKodeProgram(src.getKodeProgram());
        p.setNamaProgram(src.getNamaProgram());
        p.setIndikator(src.getIndikator());
        return p;
    }

    private RencanaKinerjaResDTO.Kegiatan mapKegiatan(DetailRekinPegawaiResDTO.Kegiatan src) {
        RencanaKinerjaResDTO.Kegiatan k = new RencanaKinerjaResDTO.Kegiatan();
        k.setKodeKegiatan(src.getKodeKegiatan());
        k.setNamaKegiatan(src.getNamaKegiatan());
        k.setIndikator(src.getIndikator());
        return k;
    }

    private RencanaKinerjaResDTO.SubKegiatan mapSubKegiatan(DetailRekinPegawaiResDTO.SubKegiatan src) {
        RencanaKinerjaResDTO.SubKegiatan s = new RencanaKinerjaResDTO.SubKegiatan();
        s.setKodeSubkegiatan(src.getKodeSubkegiatan());
        s.setNamaSubkegiatan(src.getNamaSubkegiatan());
        s.setIndikator(src.getIndikator());
        return s;
    }
}

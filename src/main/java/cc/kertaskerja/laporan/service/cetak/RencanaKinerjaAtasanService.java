package cc.kertaskerja.laporan.service.cetak;

import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyDTO;
import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyResponse;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.helper.Crypto;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import cc.kertaskerja.laporan.repository.VerifikatorRepository;
import cc.kertaskerja.laporan.service.external.DetailRekinResponseDTO;
import cc.kertaskerja.laporan.service.external.JabatanService;
import cc.kertaskerja.laporan.service.global.RencanaKinerjaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RencanaKinerjaAtasanService {
    private final RencanaKinerjaAtasanRepository repository;
    private final VerifikatorRepository verifikatorRepository;
    private final JabatanService jabatanService;
    private final RencanaKinerjaService rencanaKinerjaService;

    public RencanaKinerjaHierarchyResponse getHierarchyByNipAndTahun(String encryptedNip, String tahun_verifikasi, String sessionId) {
        String decryptedNip = Crypto.decrypt(encryptedNip);

        List<RencanaKinerjaAtasan> list = repository.findByNipBawahan(decryptedNip);
        if (list.isEmpty()) {
            return RencanaKinerjaHierarchyResponse.builder()
                    .nipBawahan(decryptedNip)
                    .rencanaKinerjas(List.of())
                    .build();
        }

        String jenisItem = findJenisItemByLevel(list.getFirst().getLevelPegawai());

        List<RencanaKinerjaHierarchyResponse.ItemRekins> itemRekinList = susunItemByJenisItem(jenisItem, list);

        Integer totalPagu = itemRekinList.stream().map(RencanaKinerjaHierarchyResponse.ItemRekins::getPagu)
                .reduce(0, Integer::sum);

        // find atasan dari status rencana kinerja
        // tambah kode opd kalau perlu
        int tahunInt = Integer.parseInt(tahun_verifikasi);
        List<Verifikator> listAtasan = verifikatorRepository.findByNipAndTahunVerifikasi(encryptedNip, tahunInt);
        if (listAtasan.size() != 1) {
            throw new IllegalStateException("Atasan belum dipilih");
        }
        Verifikator atasan = listAtasan.getFirst();
        String namaAtasan = atasan.getNamaAtasan();
        String nipAtasan = Crypto.decrypt(atasan.getNipAtasan());
        String jabatanAtasan = jabatanService.jabatanUser(sessionId, nipAtasan).getNamaJabatan();


        // find data for setup bawahan
        RencanaKinerjaAtasan first = list.getFirst();
        String nipBawahan = first.getNipBawahan();
        String namaBawahan = first.getNamaBawahan();
        Integer levelPegawai = first.getLevelPegawai();
        String jabatanBawahan = jabatanService.jabatanUser(sessionId, nipBawahan).getNamaJabatan();

        // Group by parent id
        List<RencanaKinerjaHierarchyDTO> grouped = list.stream()
                .collect(Collectors.groupingBy(RencanaKinerjaAtasan::getIdRencanaKinerja))
                .values().stream()
                .map(entry -> {
                    var parent = entry.getFirst();
                    var children = entry.stream()
                            .filter(a -> a.getIdRencanaKinerjaBawahan() != null)
                            .map(a -> {
                                DetailRekinResponseDTO detail = rencanaKinerjaService.getDetailRekin(sessionId, a.getIdRencanaKinerjaBawahan());
                                // indikator response
                                var indRekins = Optional.ofNullable(detail)
                                        .map(DetailRekinResponseDTO::getRencanaKinerja)
                                        .map(DetailRekinResponseDTO.RencanaKinerjaItem::getIndikator)
                                        .filter(inds -> !inds.isEmpty())
                                        .orElseGet(() -> {
                                            var dummyTarget = new DetailRekinResponseDTO.RencanaKinerjaItem.IndikatorRekin.TargetIndikator();
                                            dummyTarget.setTarget("-");
                                            dummyTarget.setSatuan("-");

                                            var dummyInd = new DetailRekinResponseDTO.RencanaKinerjaItem.IndikatorRekin();
                                            dummyInd.setNamaIndikator("-");
                                            dummyInd.setTargets(List.of(dummyTarget));
                                            return List.of(dummyInd);
                                        });

                                // map to indikator sasaran kinerja
                                var indikatorSasaran = indRekins.stream()
                                        .map(ind -> Optional.ofNullable(ind.getTargets())
                                                .filter(t -> !t.isEmpty())
                                                .map(List::getFirst)
                                                .map(target -> RencanaKinerjaHierarchyDTO.ChildDTO.IndikatorSasaran.builder()
                                                        .indikator(Optional.ofNullable(ind.getNamaIndikator()).orElse("-"))
                                                        .target(Optional.ofNullable(target.getTarget()).orElse("-"))
                                                        .satuan(Optional.ofNullable(target.getSatuan()).orElse("-"))
                                                        .build())
                                                .orElseGet(() -> RencanaKinerjaHierarchyDTO.ChildDTO.IndikatorSasaran.builder()
                                                        .indikator(Optional.ofNullable(ind.getNamaIndikator()).orElse("-"))
                                                        .target("-")
                                                        .satuan("-")
                                                        .build()))
                                        .toList();

                                return RencanaKinerjaHierarchyDTO.ChildDTO.builder()
                                        .idRencanaKinerjaBawahan(a.getIdRencanaKinerjaBawahan())
                                        .rencanaKinerjaBawahan(a.getNamaRencanaKinerjaBawahan())
                                        .indikatorSasarans(indikatorSasaran)
                                        .build();
                            })
                            .toList();

                    return RencanaKinerjaHierarchyDTO.builder()
                            .idRencanaKinerjaAtasan(parent.getIdRencanaKinerja())
                            .rencanaKinerjaAtasan(parent.getNamaRencanaKinerja())
                            .rekinBawahans(children)
                            .build();
                })
                .toList();
        return RencanaKinerjaHierarchyResponse.builder()
                .nipAtasan(nipAtasan)
                .namaAtasan(namaAtasan)
                .jabatanAtasan(jabatanAtasan)
                .nipBawahan(nipBawahan)
                .namaBawahan(namaBawahan)
                .jabatanBawahan(jabatanBawahan)
                .rencanaKinerjas(grouped)
                .levelPegawai(levelPegawai)
                .jenisItem(jenisItem)
                .itemRekins(itemRekinList)
                .totalPagu(totalPagu)
                .build();
    }

    private List<RencanaKinerjaHierarchyResponse.ItemRekins> susunItemByJenisItem(String jenisItem, List<RencanaKinerjaAtasan> list) {
        Stream<RencanaKinerjaHierarchyResponse.ItemRekins> stream =  list.stream()
                .map(rk -> {
                    if (Objects.equals(jenisItem, "Program")) {
                        return RencanaKinerjaHierarchyResponse.ItemRekins.builder()
                                .kodeItem(rk.getKodeProgram())
                                .namaItem(rk.getProgram())
                                .pagu(rk.getPaguAnggaran())
                                .build();
                    } else if (Objects.equals(jenisItem, "Sub Kegiatan/Kegiatan")) {
                        String kodeKegiatanSubkegiatan = rk.getKodeSubKegiatan() + "/" + rk.getKodeKegiatan();
                        String kegiatanSubkegiatan = rk.getSubKegiatan() + "/" + rk.getKegiatan();
                        return RencanaKinerjaHierarchyResponse.ItemRekins.builder()
                                .kodeItem(kodeKegiatanSubkegiatan)
                                .namaItem(kegiatanSubkegiatan)
                                .pagu(rk.getPaguAnggaran())
                                .build();
                    }
                    return RencanaKinerjaHierarchyResponse.ItemRekins.builder()
                            .kodeItem("")
                            .namaItem("")
                            .pagu(rk.getPaguAnggaran())
                            .build();
                });
        // filter duplicate logic
        // only program can have multiple item
        if (Objects.equals(jenisItem, "Sub Kegiatan/Kegiatan")) {
            stream = stream.filter(distinctByKey(RencanaKinerjaHierarchyResponse.ItemRekins::getKodeItem));
        }

        return stream.toList();
    }

    private String findJenisItemByLevel(Integer level) {
        // level pokin atasan
        // 4 -> kepala dinas, dimiliki oleh bawahan kabid
        // 5 -> kabid, dimilik oleh subkoor
        // 6 -> subkoor, dimiliki oleh staff
        return switch (level) {
            case 4 -> "Program";
            case 5 -> "Sub Kegiatan/Kegiatan";
            default -> "";
        };
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        var seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}

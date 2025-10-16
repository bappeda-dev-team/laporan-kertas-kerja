package cc.kertaskerja.laporan.service.cetak;

import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyDTO;
import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyResponse;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.entity.Verifikator;
import cc.kertaskerja.laporan.helper.Crypto;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import cc.kertaskerja.laporan.repository.VerifikatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RencanaKinerjaAtasanService {
    private final RencanaKinerjaAtasanRepository repository;
    private final VerifikatorRepository verifikatorRepository;

    public RencanaKinerjaHierarchyResponse getHierarchyByNipAndTahun(String encryptedNip, String tahun_verifikasi) {
        String decryptedNip = Crypto.decrypt(encryptedNip);

        List<RencanaKinerjaAtasan> list = repository.findByNipBawahan(decryptedNip);
        if (list.isEmpty()) {
            return RencanaKinerjaHierarchyResponse.builder()
                    .nipBawahan(decryptedNip)
                    .rencanaKinerjas(List.of())
                    .build();
        }

        // find atasan dari status rencana kinerja
        // tambah kode opd kalau perlu
        List<Verifikator> listAtasan = verifikatorRepository.findByNipAndTahunVerifikasi(encryptedNip, tahun_verifikasi);
        if (listAtasan.size() != 1) {
            throw new IllegalStateException("Atasan belum dipilih");
        }
        Verifikator atasan = listAtasan.getFirst();
        String namaAtasan = atasan.getNamaAtasan();
        String nipAtasan = atasan.getNipAtasan();

        // find data for setup bawahan
        RencanaKinerjaAtasan first = list.getFirst();
        String nipBawahan = first.getNipBawahan();
        String namaBawahan = first.getNamaBawahan();

        // Group by parent id
        List<RencanaKinerjaHierarchyDTO> grouped =  list.stream()
                .collect(Collectors.groupingBy(RencanaKinerjaAtasan::getIdRencanaKinerja))
                .values().stream()
                .map(entry -> {
                    var parent = entry.getFirst();
                    var children = entry.stream()
                            .filter(a -> a.getIdRencanaKinerjaBawahan() != null)
                            .map(a -> RencanaKinerjaHierarchyDTO.ChildDTO.builder()
                                    .idRencanaKinerjaBawahan(a.getIdRencanaKinerjaBawahan())
                                    .rencanaKinerjaBawahan(a.getNamaRencanaKinerjaBawahan())
                                    .build())
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
                .nipBawahan(nipBawahan)
                .namaBawahan(namaBawahan)
                .rencanaKinerjas(grouped)
                .build();
    }
}

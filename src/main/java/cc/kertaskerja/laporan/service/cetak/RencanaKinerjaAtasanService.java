package cc.kertaskerja.laporan.service.cetak;

import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyDTO;
import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyResponse;
import cc.kertaskerja.laporan.entity.RencanaKinerjaAtasan;
import cc.kertaskerja.laporan.helper.Crypto;
import cc.kertaskerja.laporan.repository.RencanaKinerjaAtasanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RencanaKinerjaAtasanService {
    private final RencanaKinerjaAtasanRepository repository;

    public RencanaKinerjaHierarchyResponse getHierarchyByNipAndTahun(String encryptedNip) {
        String decryptedNip = Crypto.decrypt(encryptedNip);

        List<RencanaKinerjaAtasan> list = repository.findByNipBawahan(decryptedNip);
        if (list.isEmpty()) {
            return RencanaKinerjaHierarchyResponse.builder()
                    .nipBawahan(decryptedNip)
                    .rencanaKinerjas(List.of())
                    .build();
        }

        // first data for setup nama atasan bawahan
        RencanaKinerjaAtasan first = list.getFirst();
        String nipAtasan = first.getNip();
        String namaAtasan = first.getNama();
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

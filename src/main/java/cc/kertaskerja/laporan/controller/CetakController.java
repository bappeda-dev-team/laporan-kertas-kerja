package cc.kertaskerja.laporan.controller;

import cc.kertaskerja.laporan.dto.cetak.RencanaKinerjaHierarchyResponse;
import cc.kertaskerja.laporan.service.cetak.RencanaKinerjaAtasanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cetak")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CetakController {
    private final RencanaKinerjaAtasanService service;

    @GetMapping("/perjanjian-kinerja/{encryptedNip}")
    public ResponseEntity<RencanaKinerjaHierarchyResponse> cetakPk(@PathVariable String encryptedNip) {
        var result = service.getHierarchyByNipAndTahun(encryptedNip);
        return ResponseEntity.ok(result);
    }
}
